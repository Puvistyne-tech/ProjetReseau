package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * MESSAGE_PRIVATE(5) =
 * 5 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * msg (STRING<=1024)
 */
public class PrivateMessagePacket implements Packet {

    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String severDestination;
    private final String loginDestination;
    private final String message;

    public PrivateMessagePacket(
            String serverSource,
            String loginSource,
            String severDestination,
            String loginDestination,
            String message) {
        this.opcode = OPCODE.MESSAGE_PRIVATE;
        this.serverSource = Packet.verifySize(serverSource, 100);
        this.loginSource = Packet.verifySize(loginSource, 30);
        this.severDestination = Packet.verifySize(severDestination, 100);
        this.loginDestination = Packet.verifySize(loginDestination, 30);
        this.message = Packet.verifySize(message, 1024);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = new Buffer.Builder(opcode)
                .addStringPacket(new StringPacket(serverSource))
                .addStringPacket(new StringPacket(loginSource))
                .addStringPacket(new StringPacket(severDestination))
                .addStringPacket(new StringPacket(loginDestination))
                .addStringPacket(new StringPacket(message))
                .build();
//        return buffer.flip();
        return buffer;
    }
}
