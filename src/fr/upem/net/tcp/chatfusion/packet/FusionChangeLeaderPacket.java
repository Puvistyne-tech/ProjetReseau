package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * <p>
 *     Create a FUSION_CHANGE_LEADER packet.
 *</p>
 * <p>
 *     Create this packet in order to specify from a server
 *     to his clients that he will no longer be the leader
 *     of the new mega-server.
 * </p>
 * @param leader  the new leader server name
 * @param address the new leader server address
 */
public record FusionChangeLeaderPacket(String leader, InetSocketAddress address) implements Packet {

    /**
     * Create a byte buffer according to FUSION_CHANGE_LEADER format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_CHANGE_LEADER)
                .addString(leader)
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
