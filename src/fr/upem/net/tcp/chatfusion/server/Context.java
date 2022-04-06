package fr.upem.net.tcp.chatfusion.server;


import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.reader.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.logging.Logger;


public class Context implements IContext {

    Logger logger = Logger.getLogger(Context.class.getName());


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

        for (; ; ) {
            var opCodeReader = OpCodeHandler.getOpCode(bufferIn);
            ///esijsopie

            Reader<? extends Packet> reader = null;

//            List<? extends Constable> list=new ArrayList<String>();
//            list.add("new PublicMessagePackets");
//            list.add(2);

            switch (opCodeReader) {
                case MESSAGE_PUBLIC -> {
                    reader = new PublicMessageReader();
                    break;
                }
                case MESSAGE_PRIVATE -> {
                    reader = new PrivateMessageReader();
                    break;
                }
            }

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
            return;
        }
        processIn();
        updateInterestOps();

    }

    @Override
    public void doWrite() throws IOException {
        bufferOut.flip();
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
