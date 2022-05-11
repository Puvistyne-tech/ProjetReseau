package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.FusionInitKOPacket;
import fr.upem.net.tcp.chatfusion.packet.FusionInitOKPacket;
import fr.upem.net.tcp.chatfusion.packet.LoginRefusedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * FUSION_INIT_KO(10)= 10 (OPCODE)
 */

/**
 * Read a FUSION_INIT_KO request
 */
public class FusionInitKOReader implements Reader<FusionInitKOPacket> {
    private final ByteReader byteReader = new ByteReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_INIT_KO format
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
        var status = byteReader.process(buffer);
        if (ProcessStatus.DONE == status) {
            if (OPCODE.byteToOpcode(byteReader.get()) != OPCODE.FUSION_INIT_KO) {
                throw new BadPacketReceivedException();
            }
        }
        return status;
    }
    /**
     * Get a FUSION_INIT_KO packet
     * @return the FUSION_INIT_KO packet
     */
    @Override
    public FusionInitKOPacket get() {
        return new FusionInitKOPacket();
    }

    /**
     * Reset the FusionInitKOReader's fields
     */
    @Override
    public void reset() {
        byteReader.reset();
    }
}
