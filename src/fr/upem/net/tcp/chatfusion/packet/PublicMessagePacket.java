package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * MESSAGE(4) =
 * 4 (OPCODE)
 * server (STRING<=100)
 * login (STRING<=30)
 * msg (STRING<=1024)
 */
public class PublicMessagePacket implements Packet {

    private final OPCODE opcode;
    private final String server;
    private final String login;
    private final String message;

    public PublicMessagePacket(
            String serverSource,
            String loginSource,
            String message)  {
        //TODO OPCODE
        this.opcode = OPCODE.MESSAGE_PUBLIC;
        this.server = Packet.verifySize(serverSource, 100);
        this.login = Packet.verifySize(loginSource, 30);
        this.message = Packet.verifySize(message, 1024);
    }

    @Override
    public ByteBuffer toByteBuffer()  {
        var buffer = new Buffer.Builder(opcode)
                .addString(new StringPacket(server).toByteBuffer())
                .addString(new StringPacket(login).toByteBuffer())

                .addString(new StringPacket(message).toByteBuffer())
                .build();

        return buffer;
    }
}
