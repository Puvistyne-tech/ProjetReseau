package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * <p>
 *     Create a FUSION_INIT_FWD packet.
 *</p>
 * <p>
 *     Create this packet in order to specify, from
 *     the current server, the mega-server leader.
 * </p>
 * @param leaderAddress the leader server address
 */
public record FusionInitFWDPacket(InetSocketAddress leaderAddress) implements Packet {

    /**
     * Create a byte buffer according to FUSION_INIT_FWD format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_INIT_FWD)
                .addString(leaderAddress.toString())
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
