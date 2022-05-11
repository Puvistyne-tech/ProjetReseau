package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * FUSION_INIT(8) = 8 (OPCODE)
 * name (STRING<=100)
 * adresse (SOCKETADDRESS)
 * nb_members (INT)
 * name_0 (STRING<=100)
 * name_1 ...
 */

/**
 * <p>
 *     Create a FUSION_INIT packet.
 *</p>
 * <p>
 *     Create this packet in order to initiate, from a server
 *     a server to another server, a fusion.
 * </p>
 */
public class FusionInitPacket implements Packet {
    private final OPCODE opcode;
    private final String serverSrc;
    private final InetSocketAddress socketAddress;
    private final List<String> members;

    /**
     * Constructs a FUSION_INIT packet according to
     * FUSION_INIT format
     * @param serverSrc     the source server name
     * @param socketAddress the source server address
     * @param members       the mega-server's members
     */
    public FusionInitPacket(
            String serverSrc,
            InetSocketAddress socketAddress,
            List<String> members) {
        this.opcode = OPCODE.FUSION_INIT;
        this.serverSrc = Packet.verifySize(serverSrc, 100);
        this.socketAddress = socketAddress;
        this.members = members;
    }

    /**
     * Get the source server name
     * @return the source server name
     */
    public String getServerSrc() {
        return serverSrc;
    }

    /**
     * Get the source server address
     * @return the source server address
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Get the members of the mega-server
     * @return the member's list
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * Create a byte buffer according to FUSION_INIT format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {

        var tmpBuff = ByteBuffer.allocate(1024);
        for (var mem : members) {
            var tmp = new StringPacket(mem).toByteBuffer();
            tmpBuff.put(tmp);
        }

        return new Buffer.Builder(opcode)
                .addStringPacket(new StringPacket(serverSrc))
                .addStringPacket(new StringPacket(socketAddress.toString()))
                .addInt(members.size())
                .addBuffer(tmpBuff)
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
