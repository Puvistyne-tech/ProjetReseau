package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;
import org.w3c.dom.ls.LSInput;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private final List<String> members;

    public FusionInitPacket(
            String name,
            SocketAddress address,
            int nbMembers,
            List<String> members) {
        this.opcode = OPCODE.FUSION_INIT;
        this.name = Packet.verifySize(name, 100);
        this.address = address;
        this.nbMembers = nbMembers;
        this.members = members;
    }


    @Override
    public ByteBuffer toByteBuffer() {

        var tmpBuff = ByteBuffer.allocate(1024);
        for (var mem : members) {
            var tmp = new StringPacket(mem).toByteBuffer();
            tmpBuff.put(tmp);
        }

        return new Buffer.Builder(opcode)
                .addStringPacket(new StringPacket(name))
                .addStringPacket(new StringPacket(address.toString()))
                .addInt(nbMembers)
                .addBuffer(tmpBuff)
                .build();
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);

    }
}
