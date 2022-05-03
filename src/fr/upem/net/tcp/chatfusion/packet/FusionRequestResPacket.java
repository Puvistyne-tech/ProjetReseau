package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * FUSION_REQUEST_RES(13) =
 * 13 (OPCODE)
 * status (BYTE)
 */
public class FusionRequestResPacket implements Packet {
    private final Byte status;

    public FusionRequestResPacket(Byte status) {
        this.status = status;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_REQUEST_RESP)
                .addBytes(status)
                .build();
    }
    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
