package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * FUSION_INIT_KO(10)= 10 (OPCODE)
 */

/**
 * <p>
 *     Create a FUSION_INIT_KO packet.
 *</p>
 * <p>
 *     Create this packet in order to specify, from a server to
 *     another server, that the FUSION_INIT request has been aborted.
 * </p>
 */
public class FusionInitKOPacket implements Packet {
    /**
     * Create a byte buffer according to FUSION_INIT_KO format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_INIT_KO).build();
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
