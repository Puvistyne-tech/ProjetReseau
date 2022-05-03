package fr.upem.net.tcp.chatfusion.visitor;

import fr.upem.net.tcp.chatfusion.client.Client;
import fr.upem.net.tcp.chatfusion.client.ClientChatFusion;
import fr.upem.net.tcp.chatfusion.client.Context;
import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.*;

import java.io.Reader;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientVisitor implements IPacketVisitor {

    private final ClientChatFusion client;
    private final Context context;
    private boolean authenticated = false;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");


    public ClientVisitor(Context context) {
        this.client = null;
        this.context = context;
    }

    @Override
    public void visit(Packet packet) {
        switch (packet) {
            case LoginAcceptedPacket value -> {
                System.out.println("You have successfully established the connection with the server " + value.server());
                context.setServerName(value.server());
                authenticated = true;
            }
            case LoginRefusedPacket value -> {
                System.out.println("Server has refused your connection. Now closing ...");
                //TODO methods to reconnect with new credentials
                authenticated = false;
                //TODO
//                context.silentlyClose();
            }
            case PublicMessagePacket value -> {
                System.out.println("[ " + value.server() + " ] " + value.login() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.message());
            }
            case PrivateMessagePacket value -> {
                System.out.println("[ " + value.serverSource() + " ] " + value.loginSource() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.message());
            }
            case PrivateFilePacket value -> {
                System.out.println("[ " + value.getServerSource() + " ] " + value.getLoginSource() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.getFilename());
                ReceiveFile(value.getNbBlocks(), value.getBlockSize(), value.getBytes());
            }
            default -> throw new BadPacketReceivedException();
        }
    }

    private void ReceiveFile(int nbBlocks, int blockSize, ByteBuffer bytes) {
        //Receive File and save it
    }
}
