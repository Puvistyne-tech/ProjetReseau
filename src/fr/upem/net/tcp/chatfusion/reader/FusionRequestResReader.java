package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionRequestResPacket;
import java.nio.ByteBuffer;

/**
 * FUSION_REQUEST_RES(13) = 13 (OPCODE)
 * status (BYTE)
 */

/**
 * Read a FUSION_REQUEST_RESP request
 */
public class FusionRequestResReader implements Reader<FusionRequestResPacket> {
    private final ByteReader byteReader = new ByteReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_REQUEST_RESP format
     * </p>
     * <p>
     *     According to the convention, the byte buffer is in writing mode
     *     previously and afterward the method call
     * </p>
     * @param buffer the byte buffer
     * @return       the byte buffer status
     */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return byteReader.process(buffer);
    }

    /**
     * Get a FUSION_REQUEST_RESP packet
     * @return the FUSION_REQUEST_RESP packet
     */
    @Override
    public FusionRequestResPacket get() {
        return new FusionRequestResPacket(byteReader.get());
    }

    /**
     * Reset the FusionRequestResReader's fields
     */
    @Override
    public void reset() {
        byteReader.reset();
    }
}
