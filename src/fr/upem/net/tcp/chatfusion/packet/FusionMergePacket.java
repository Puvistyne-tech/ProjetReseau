package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * FUSION_MERGE(15)
 * 15 (OPCODE)
 * name (STRING)
 */

/**
 * <p>
 *     Create a FUSION_MERGE packet.
 *</p>
 * <p>
 *     Create this packet in order to initiate, from a server
 *     to the new mega-server leader, a fusion.
 * </p>
 * @param name the server name that connects to the new leader
 */
public record FusionMergePacket(String name) implements Packet {

    /**
     * Create a byte buffer according to FUSION_MERGE format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_MERGE)
                .addString(name)
                .build();
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
