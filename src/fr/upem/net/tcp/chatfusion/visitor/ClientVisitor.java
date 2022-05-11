package fr.upem.net.tcp.chatfusion.visitor;

import fr.upem.net.tcp.chatfusion.client.ClientChatFusion;
import fr.upem.net.tcp.chatfusion.client.Context;
import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardOpenOption.*;

public class ClientVisitor implements IPacketVisitor {

    private final ClientChatFusion client;
    private final Context context;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");


    public ClientVisitor(ClientChatFusion client, Context context) {
        this.client = client;
        this.context = context;
    }

    @Override
    public void visit(Packet packet) {
        if (client.isLoggedIn()) {
            switch (packet) {
                case PublicMessagePacket value -> {
                    System.out.println("[ " + value.serverSource() + " ] " + value.loginSource() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.message());
                }
                case PrivateMessagePacket value -> {
                    System.out.println("[ " + value.serverSource() + " ] " + value.loginSource() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.message());
                }
                case PrivateFilePacket value -> {
                    System.out.println("[ " + value.getServerSource() + " ] " + value.getLoginSource() + " :: " + dtf.format(LocalDateTime.now()) + " = " + value.getFilename());
                    ReceiveFile(value);
                }
                default -> throw new BadPacketReceivedException();
            }
        } else {
            switch (packet) {
                case LoginAcceptedPacket value -> {
                    System.out.println("You have successfully established the connection with the server " + value.server());
                    context.setServerName(value.server());
                    client.LogIn();
                }
                case LoginRefusedPacket value -> {
                    System.out.println("Server has refused your connection. Now closing ...");
                    client.LogOut();
                    context.close();
                }
                default -> throw new BadPacketReceivedException();
            }
        }
    }

    private void ReceiveFile(PrivateFilePacket packet) {
        //Receive File and save it
        var size = packet.getBlockSize();
        try (
                //TODO path
                var outChannel = FileChannel.open(Path.of(packet.getFilename()), WRITE, CREATE, TRUNCATE_EXISTING);
        ) {
            while (packet.getBytes().hasRemaining()) {
                outChannel.write(packet.getBytes());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(packet);
    }
}
