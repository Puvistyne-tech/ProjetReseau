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
public class FusionMergePacket implements Packet {
    private final String name;

    public FusionMergePacket(String name) {
        this.name = name;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_MERGE)
                .addString(name)
                .build();
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
