package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionRequestPacket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * FUSION_REQUEST(12) =
 * 12 (OPCODE)
 * address (SOCKETADDRESS)
 */

/**
 * Read a FUSION_REQUEST request
 */
public class FusionRequestReader implements Reader<FusionRequestPacket> {

    private final StringReader stringReader = new StringReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_REQUEST format
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
     * Get a FUSION_REQUEST packet
     * @return the FUSION_REQUEST packet
     */
    @Override
    public FusionRequestPacket get() {
        var t = stringReader.get().message();
        return new FusionRequestPacket(
//                new InetSocketAddress(
//                        url(t).getHost(), url(t).getPort()
//                )
                getAddress(t)
        );
    }

    /**
     * Reset the FusionRequestReader's fields
     */
    @Override
    public void reset() {
        stringReader.reset();
    }
}
