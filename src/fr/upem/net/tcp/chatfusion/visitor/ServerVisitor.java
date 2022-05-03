package fr.upem.net.tcp.chatfusion.visitor;

import fr.upem.net.tcp.chatfusion.server.Context;
import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.*;
import fr.upem.net.tcp.chatfusion.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerVisitor implements IPacketVisitor {

    private final Server server;
    private final Context context;
    private boolean authenticated = false;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");


    public ServerVisitor(Context context) {
        this.context = context;
        this.server = context.getServer();
    }

    @Override
    public void visit(Packet packet) {
        switch (packet) {
            case LoginAnonymousPacket clientP -> {
                var client = clientP.getLogin();
                if (server.ifClientAlreadyConnected(client)) {
                    server.sendTo(context.getKey(), new LoginRefusedPacket());
//                    context.silentlyClose();
                } else {
                    context.login = client;
                    try {
                        server.addClients(client, context.getSocketChannel().getRemoteAddress());
                        server.sendTo(context.getKey(), new LoginAcceptedPacket(server.getLeaderName()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case LoginPasswordPacket clientP -> {
                var client = clientP.getLogin();
                if (server.ifClientAlreadyConnected(client)) {
                    server.sendTo(context.getKey(), new LoginRefusedPacket());
//                    context.silentlyClose();
                } else {
                    context.login = client;
                    try {
                        server.addClients(client, context.getSocketChannel().getRemoteAddress());
                        server.sendTo(context.getKey(), new LoginAcceptedPacket(server.getLeaderName()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case PublicMessagePacket value -> {
                server.broadcast(value);
            }
            case PrivateMessagePacket prvtMsgP -> {
                var loginDest = prvtMsgP.loginDestination();
                var kk = server.getClientAddress(loginDest);
                if (kk != null) {
                    server.sendTo(kk, prvtMsgP);
                }
            }
            case PrivateFilePacket value -> {
                System.out.println("(" + value.getServerSource() + " : " + value.getLoginSource() + ")" + dtf.format(LocalDateTime.now()) + " ::: " + value.getFilename());
                ReceiveFile(value.getNbBlocks(), value.getBlockSize(), value.getBytes());
            }
            default -> throw new BadPacketReceivedException();
        }
    }

    private void ReceiveFile(int nbBlocks, int blockSize, ByteBuffer bytes) {
        //Receive File and save it
    }
}
