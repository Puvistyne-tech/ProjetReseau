package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * LOGIN_REFUSED(3)
 * OPCODE 3
 */

/**
 * <p>
 *     Create a LOGIN_REFUSED packet.
 *</p>
 * <p>
 *     Create this packet in order to specify to the
 *     remote server that the login failed.
 * </p>
 */
public class LoginRefusedPacket implements Packet {

    /**
     * Create a byte buffer according to LOGIN_REFUSED format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder().addOpCode(OPCODE.LOGIN_REFUSED).build();
    }

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
