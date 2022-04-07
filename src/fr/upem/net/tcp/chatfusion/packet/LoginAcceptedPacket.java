package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * LOGIN_ACCEPTED(2) = 2 (OPCODE)
 * serveur name (STRING<=100)
 */
public class LoginAcceptedPacket implements Packet {
    private final OPCODE opcode;
    private final String server;

    public LoginAcceptedPacket(String server) {
        opcode = OPCODE.LOGIN_ACCEPTED;
        this.server = Packet.verifySize(server, 100);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(server)
                .build();
    }
}
