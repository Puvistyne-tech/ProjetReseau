package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * FUSION_REQUEST_RES(13) =
 * 13 (OPCODE)
 * status (BYTE)
 */

/**
 * <p>
 *     Create a FUSION_REQUEST_RESP packet.
 *</p>
 * <p>
 *     Create this packet in order to respond to a FUSION_REQUEST request.
 * </p>
 * @param status the byte which indicates whether the merge is possible or
 *               has been initiated
 */
public record FusionRequestResPacket(Byte status) implements Packet {

    /**
     * Create a byte buffer according to FUSION_REQUEST_RESP format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(OPCODE.FUSION_REQUEST_RESP)
                .addBytes(status)
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
