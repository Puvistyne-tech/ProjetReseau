package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.LoginAcceptedPacket;
import fr.upem.net.tcp.chatfusion.packet.LoginRefusedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * Read a LOGIN_REFUSED request
 */
public class LoginRefusedReader implements Reader<LoginRefusedPacket> {

    private final ByteReader byteReader = new ByteReader();

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     LOGIN_REFUSED format
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
            if (OPCODE.byteToOpcode(byteReader.get()) != OPCODE.LOGIN_REFUSED) {
                throw new BadPacketReceivedException();
            }
        }
        return status;
    }

    /**
     * Get a LOGIN_REFUSED request
     * @return the LOGIN_REFUSED request
     */
    @Override
    public LoginRefusedPacket get() {
        return new LoginRefusedPacket();
    }

    /**
     * Reset the LoginRefusedReader's fields
     */
    @Override
    public void reset() {
        byteReader.reset();
    }
}
