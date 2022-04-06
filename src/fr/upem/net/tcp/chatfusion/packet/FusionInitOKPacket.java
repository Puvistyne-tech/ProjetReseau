package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class FusionInitOKPacket implements Packet {
    private final OPCODE opcode;
    private final String name;
    private final SocketAddress address;
    private final int nbMembers;
    private final String[] members;

    public FusionInitOKPacket(
            String name,
            SocketAddress address,
            int nbMembers,
            String... members)  {
        this.opcode = OPCODE.FUSION_INIT_OK;
        this.name = Packet.verifySize(name, 100);
        this.address = address;
        this.nbMembers = nbMembers;
        this.members = members;
    }

    @Override
    public ByteBuffer toByteBuffer()  {
        return new FusionInitPacket(
                name,
                address,
                nbMembers,
                members).toByteBuffer();
    }

}
