package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginPasswordPacket;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

/**
 * Read a LOGIN_PASSWORD request
 */
public class LoginPasswordReader implements Reader<LoginPasswordPacket> {

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_USERNAME}
     * {@link #WAITING_PASSWORD}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * LOGIN_PASSWORD format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login username
         */
        WAITING_USERNAME,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login password
         */
        WAITING_PASSWORD,
        /**
         * The byte buffer hasn't been filled according to the
         * LOGIN_PASSWORD format
         */
        ERROR
    }

    private State state = State.WAITING_USERNAME;
    private final StringReader stringReader = new StringReader();
    private LoginPasswordPacket message;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     LOGIN_PASSWORD format
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
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        String login = null;
        String password=null;

        if (state == State.WAITING_USERNAME) {
            login = readString(buffer, State.WAITING_PASSWORD);
        }
        if (state == State.WAITING_PASSWORD) {
            password = readString(buffer, State.DONE);
        } else {
            return REFILL;
        }

        if (login == null || password == null) {
            return ERROR;
        }

        message = new LoginPasswordPacket(login, password);

        state = State.DONE;

        return DONE;
    }

    /**
     * Read the content of the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the LOGIN_PASSWORD format
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to LOGIN_PASSWORD format
     * @return          the string read in the byte buffer
     */
    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            message = null;
            stringReader.reset();
            state = nextState;
            return value.message();
        } else return null;
    }

    /**
     * Get the value of the LOGIN_PASSWORD request
     * @return the LOGIN_PASSWORD request
     */
    @Override
    public LoginPasswordPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    /**
     * Reset the LoginPasswordReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING_USERNAME;
        stringReader.reset();
        message = null;
    }
}
