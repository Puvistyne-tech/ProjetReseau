package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionMergePacket;

import java.nio.ByteBuffer;

/**
 * FUSION_MERGE(15) = 15 (OPCODE)
 * name (STRING)
 */

/**
 * Read a FUSION_MERGE request
 */
public class FusionMergeReader implements Reader<FusionMergePacket> {

    private final StringReader stringReader = new StringReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_MERGE format
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
        return stringReader.process(buffer);
    }

    /**
     * Get a FUSION_MERGE packet
     * @return the FUSION_MERGE packet
     */
    @Override
    public FusionMergePacket get() {
        return new FusionMergePacket(stringReader.get().message());
    }

    /**
     * Reset the FusionMergeReader's fields
     */
    @Override
    public void reset() {
        stringReader.reset();
    }
}
