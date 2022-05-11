package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * <p>
 *     Create a FUSION_REQUEST packet.
 *</p>
 * <p>
 *     Create this packet in order to request, from a server
 *     to his leader, a fusion with a server.
 * </p>
 * @param address the server address to request the merge with
 */
public record FusionRequestPacket(InetSocketAddress address) implements Packet {

    /**
     * Create a byte buffer according to FUSION_REQUEST format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_REQUEST)
                .addString(address.toString())
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
