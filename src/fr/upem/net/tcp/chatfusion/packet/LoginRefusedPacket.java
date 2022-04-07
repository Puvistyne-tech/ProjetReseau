package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

public class LoginRefusedPacket implements Packet {
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.LOGIN_REFUSED).build();
    }
}
