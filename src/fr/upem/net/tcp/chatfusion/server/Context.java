package fr.upem.net.tcp.chatfusion.server;


import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.*;
import fr.upem.net.tcp.chatfusion.reader.*;
import fr.upem.net.tcp.chatfusion.utils.FileHander;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.logging.Logger;


public class Context implements IContext {

    Logger logger = Logger.getLogger(Context.class.getName());


    private String login;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final Server server;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final ArrayDeque<Packet> queue = new ArrayDeque<>();
    private boolean closed = false;

    public Context(Server server, SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.server = server;
//        this.login
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Process the content of bufferIn
     * <p>
     * The convention is that bufferIn is in write-mode before the call to process and
     * after the call
     */
    @Override
    public void processIn() {

        var opCodeReader = OpCodeHandler.getOpCode(bufferIn);
//        if (opCodeReader.ordinal() >= 0 && opCodeReader.ordinal() < 20)
        for (; ; ) {

            Reader<? extends Packet> reader = null;

            switch (opCodeReader) {
                case MESSAGE_PUBLIC -> reader = new PublicMessageReader();
                case MESSAGE_PRIVATE -> {
                    handlePrivateMessage(new PrivateMessageReader());
                    return;
                }
                case LOGIN_ANONYMOUS -> {
                    handleAnonymousLogin(new LoginAnonymousReader());
                    return;
                }
                case LOGIN_PASSWORD -> {
                    handleLoginPassword(new LoginPasswordReader());
                    return;
                }
            }

            assert reader != null;
            Reader.ProcessStatus status = reader.process(bufferIn);
            switch (status) {
                case DONE:
                    var value = reader.get();
                    server.broadcast(value);
                    reader.reset();
                    break;
                case REFILL:
                    return;
                case ERROR:
                    silentlyClose();
                    return;
            }
        }
    }

    private void handlePrivateMessage(PrivateMessageReader reader) {
        assert reader != null;
        var status = reader.process(bufferIn);

        if (status == Reader.ProcessStatus.DONE) {
            var prvtMsgP = reader.get();


            //this.server.addClients(client, sc.getRemoteAddress());
            server.sendTo(key, prvtMsgP);
        } else {
            silentlyClose();
        }
    }

    private void handleLoginPassword(LoginPasswordReader reader) {
        assert reader != null;
        var status = reader.process(bufferIn);

        try {
            if (status == Reader.ProcessStatus.DONE) {
                var clientP = reader.get();
                var client = clientP.getLogin();

//                if (FileHander.checkIfUserExist(client) || server.ifClientAlreadyConnected(client)) {
                if (server.ifClientAlreadyConnected(client)) {
                    server.sendTo(key, new LoginRefusedPacket());
                    silentlyClose();
                } else {
                    this.server.addClients(client, sc.getRemoteAddress());
                    server.sendTo(key, new LoginAcceptedPacket(server.getLeaderName()));
                }
            } else {
                silentlyClose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAnonymousLogin(Reader<LoginAnonymousPacket> reader) {
        assert reader != null;
        var status = reader.process(bufferIn);

        try {
            if (status == Reader.ProcessStatus.DONE) {
                var clientP = reader.get();
                var client = clientP.getLogin();

                if (FileHander.checkIfUserExist(client)) {
                    server.sendTo(key, new LoginRefusedPacket());
                }

                this.server.addClients(client, sc.getRemoteAddress());
                server.sendTo(key, new LoginAcceptedPacket(server.getLeaderName()));
            } else {
                silentlyClose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processOut() {
        //TODO

        while (!queue.isEmpty() && bufferIn.hasRemaining()) {
            var packet = queue.poll();
            var buff = packet.toByteBuffer();
            if (buff.remaining() <= bufferOut.remaining()) {
                bufferOut.put(buff);
            }
        }

//        while (!queue.isEmpty() && bufferOut.hasRemaining()) {
//            var msg = queue.peek();
//            if (!msg.hasRemaining()) {
//                queue.poll();
//                continue;
//            }
//            if (msg.remaining() <= bufferOut.remaining()) {
//                bufferOut.put(msg);
//            } else {
//                var oldLimit = msg.limit();
//                msg.limit(bufferOut.remaining());
//                bufferOut.put(msg);
//                msg.limit(oldLimit);
//            }
//        }
//        private void processOut() {
//            var it = queue.iterator();
//            while (it.hasNext()) {
//                var msgbuff = it.next();
//                if (bufferOut.remaining() < msgbuff.remaining())
//                    continue;
//                bufferOut.put(msgbuff);
//                it.remove();
//            }
//        }
    }

    public void queueMessage(Packet packet) {

//        var username = CHARSET.encode(packets.username());
//
//        var text = CHARSET.encode(packets.text());
//        var buffer = ByteBuffer.allocate(username.remaining() + text.remaining() + Integer.BYTES * 2);
//        buffer.putInt(username.remaining())
//                .put(username)
//                .putInt(text.remaining())
//                .put(text)
//                .flip();
//        queue.offer(buffer);
        queue.add(packet);
        processOut();
        updateInterestOps();
    }

    @Override
    public void updateInterestOps() {
        int newIterOpt = 0;
        //has remaining ya qlq chose dans le bufferIN
        if (!closed && bufferIn.hasRemaining()) {//si utlisateur n'a pas ferme la cnx on continue a lire
            newIterOpt |= SelectionKey.OP_READ;
        }

        if (bufferOut.position() != 0) {//ya qlq chose dans le bufferout donc il faut l'ecrire
            newIterOpt |= SelectionKey.OP_WRITE;
        }

        if (newIterOpt == 0) {
            silentlyClose();
        } else {
            if (key.isValid()) {
                key.interestOps(newIterOpt);
            } else {
                silentlyClose();
            }
        }
//        key.interestOps(newIterOpt);

    }


    @Override
    public void doRead() throws IOException {

        if (sc.read(bufferIn) == -1) {
            logger.warning("client disconnected!");
            closed = true;
            silentlyClose();
            return;
        }
        processIn();
        updateInterestOps();

    }

    @Override
    public void doWrite() throws IOException {
        bufferOut.flip();
        System.out.println("<==Sending something...");
        sc.write(bufferOut);
        bufferOut.compact();
        processOut();
        updateInterestOps();

    }

    @Override
    public void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }


}
