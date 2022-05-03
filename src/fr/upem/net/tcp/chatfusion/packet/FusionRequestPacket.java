package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class FusionRequestPacket implements Packet {
    private final SocketAddress address;

    public FusionRequestPacket(SocketAddress address) {
        this.address = address;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_REQUEST)
                .addString(address.toString())
                .build();
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
