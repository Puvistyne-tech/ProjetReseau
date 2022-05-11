package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginAcceptedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.DONE;

/**
 * Read a LOGIN_ACCEPTED request
 */
public class LoginAcceptedReader implements Reader<LoginAcceptedPacket> {

//    private enum State {
//        DONE, WAITING_SERVER_NAME, ERROR
//    }
//
//    private State state = State.WAITING_SERVER_NAME;
    private final StringReader stringReader = new StringReader();
    private StringPacket value;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     LOGIN_ACCEPTED format
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
     * Get a LOGIN_ACCEPTED packet
     * @return the LOGIN_ACCEPTED packet
     */
    @Override
    public LoginAcceptedPacket get() {

        return new LoginAcceptedPacket(stringReader.get().message());
    }

    /**
     * Reset the LoginAcceptedReader's fields
     */
    @Override
    public void reset() {
//        state = State.WAITING_SERVER_NAME;
        stringReader.reset();
        value = null;
    }
}
