package fr.upem.net.tcp.chatfusion.server;


import fr.upem.net.tcp.chatfusion.context.AContext;
import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.*;
import fr.upem.net.tcp.chatfusion.reader.*;
import fr.upem.net.tcp.chatfusion.visitor.ServerVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.logging.Logger;

/**
 * Context for Server
 */
public class Context extends AContext {

    private String login;

    private final ServerChatFusion serverChatFusion;

    /**
     * Gets the server chat fusion
     * @return the ChatFusion's server
     */
    public ServerChatFusion getServer() {
        return this.serverChatFusion;
    }

    /**
     * Constructs a context for a server
     * @param serverChatFusion  the ChatFusion's server
     * @param key               the server socket
     */
    public Context(ServerChatFusion serverChatFusion, SelectionKey key) {
        super(key);

        if (status == Reader.ProcessStatus.DONE) {
            var prvtMsgP = reader.get();

//            var p=new PublicMessagePacket(prvtMsgP.serverSource(),prvtMsgP.loginSource(),prvtMsgP.message());
            //var p=new PrivateMessagePacket(prvtMsgP);

            var loginDest = prvtMsgP.loginDestination();
            var kk = server.getClientAddress(loginDest);
            if (kk != null) {
                //var att=kk.
                server.sendTo(kk, prvtMsgP);
            }
            reader.reset();
        } else {
            silentlyClose();
        }
        reader.reset();
    }

    private void handleLoginPassword(LoginPasswordReader reader) {
        assert reader != null;
        var status = reader.process(bufferIn);

        try {
            if (status == Reader.ProcessStatus.DONE) {
                var clientP = reader.get();
                var client = clientP.getLogin();
                reader.reset();
//                if (FileHander.checkIfUserExist(client) || server.ifClientAlreadyConnected(client)) {
                if (server.ifClientAlreadyConnected(client)) {
                    server.sendTo(key, new LoginRefusedPacket());
                    silentlyClose();
                } else {
                    this.login = client;

                    this.server.addClients(client, sc.getRemoteAddress());
                    server.sendTo(key, new LoginAcceptedPacket(server.getLeaderName()));
                }
            } else {
                silentlyClose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
    }

    private void handleAnonymousLogin(Reader<LoginAnonymousPacket> reader) {
        assert reader != null;
        var status = reader.process(bufferIn);

        try {
            if (status == Reader.ProcessStatus.DONE) {
                var clientP = reader.get();
                var client = clientP.getLogin();
                reader.reset();
                if (server.ifClientAlreadyConnected(client)) {
                    server.sendTo(key, new LoginRefusedPacket());
                    silentlyClose();
                } else {
                    this.login = client;
                    this.server.addClients(client, sc.getRemoteAddress());
                    server.sendTo(key, new LoginAcceptedPacket(server.getLeaderName()));
                }
            } else {
                silentlyClose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.reset();
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

    }

    public void queueMessage(Packet packet) {

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

    }


    @Override
    public void doRead() throws IOException {

        if (sc.read(bufferIn) == -1) {
            logger.warning("client disconnected!");
            closed = true;
            silentlyClose();
            return;
        }
        System.out.println("==> reading " + bufferIn);
        processIn();
        updateInterestOps();

    }

    @Override
    public void doWrite() throws IOException {
        bufferOut.flip();
        System.out.println("<== Sending something..." + bufferOut);
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

    @Override
    public SelectionKey getKey() {
        return this.key;
    }


}
