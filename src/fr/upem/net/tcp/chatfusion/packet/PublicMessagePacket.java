package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * MESSAGE(4) =
 * 4 (OPCODE)
 * server (STRING<=100)
 * login (STRING<=30)
 * msg (STRING<=1024)
 */
public class PublicMessagePacket implements Packet {

    public String server() {
        return server;
    }

    public String login() {
        return login;
    }

    public String message() {
        return message;
    }

    private final OPCODE opcode;
    private final String server;


    private final String login;
    private final String message;

    public PublicMessagePacket(
            String serverSource,
            String loginSource,
            String message) {
        this.opcode = OPCODE.MESSAGE_PUBLIC;
        this.server = Packet.verifySize(serverSource, 100);
        this.login = Packet.verifySize(loginSource, 30);
        this.message = Packet.verifySize(message, 1024);
    }

    @Override
    public ByteBuffer toByteBuffer() {

        return new Buffer.Builder(opcode)
                .addStringPacket(new StringPacket(server))
                .addStringPacket(new StringPacket(login))
                .addStringPacket(new StringPacket(message))
                .build();
    }

    @Override
    public String toString() {
        return "PublicMessagePacket{" +
                "opcode=" + opcode +
                ", server='" + server + '\'' +
                ", login='" + login + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }

    public String from() {
        return login;
    }

    public String getMessage() {
        return message;
    }
}
