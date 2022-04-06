package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * FUSION_INIT(8) = 8 (OPCODE)
 * name (STRING<=100)
 * adresse (SOCKETADDRESS)
 * nb_members (INT)
 * name_0 (STRING<=100)
 * name_1 ...
 */
public class FusionInitPacket implements Packet {
    private final OPCODE opcode;
    private final String name;
    private final SocketAddress address;
    private final int nbMembers;
    private final String[] members;

    public FusionInitPacket(
            String name,
            SocketAddress address,
            int nbMembers,
            String... members)  {
        this.opcode = OPCODE.FUSION_INIT;
        this.name = Packet.verifySize(name, 100);
        this.address = address;
        this.nbMembers = nbMembers;
        this.members = members;
    }

    @Override
    public ByteBuffer toByteBuffer()  {

        var tmpBuff=ByteBuffer.allocate(1024);
        for (var mem : members) {
            var tmp=new StringPacket(mem).toByteBuffer();
            tmpBuff.put(tmp);
        }

        var buffer = new Buffer.Builder(opcode)
                .addString(new StringPacket(name).toByteBuffer())
                .addString(new StringPacket(address.toString()).toByteBuffer())
                .addInt(nbMembers)
                .addBuffer(tmpBuff)
                .build();
        return buffer;
    }
}
