package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionInitFWDPacket;

import java.nio.ByteBuffer;

/**
 * FUSION_INIT_FWD(11)=
 * 11 (OPCODE)
 * addresse_leader (SOCKETADDRESS)
 */

/**
 * Read a FUSION_INIT_FWD request
 */
public class FusionInitFWDReader implements Reader<FusionInitFWDPacket> {
    private final StringReader stringReader = new StringReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_INIT_FWD format
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
     * Get a FUSION_INIT_FWD packet
     * @return the FUSION_INIT_FWD packet
     */
    @Override
    public FusionInitFWDPacket get() {
        var t = stringReader.get().message();
        return new FusionInitFWDPacket(
//                new InetSocketAddress(
//                        url(t).getHost(), url(t).getPort()
//                )
                getAddress(t)
        );
    }

    /**
     * Reset the FusionInitFWDReader's fields
     */
    @Override
    public void reset() {
        stringReader.reset();
    }
}
