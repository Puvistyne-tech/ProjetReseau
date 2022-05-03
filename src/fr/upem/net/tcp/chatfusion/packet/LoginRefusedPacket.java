package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * LOGIN_REFUSED(3)
 * OPCODE 3
 */
public class LoginRefusedPacket implements Packet {
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.LOGIN_REFUSED).build();
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
