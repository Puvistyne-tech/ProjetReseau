package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class FusionChangeLeaderPacket implements Packet {
    private final SocketAddress leaderAddress;

    public FusionChangeLeaderPacket(SocketAddress leaderAddress) {
        this.leaderAddress = leaderAddress;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_CHANGE_LEADER)
                .addString(leaderAddress.toString())
                .build();
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
