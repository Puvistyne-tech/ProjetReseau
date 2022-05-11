package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

/**
 * MESSAGE(4) =
 * 4 (OPCODE)
 * server (STRING<=100)
 * login (STRING<=30)
 * msg (STRING<=1024)
 */

/**
 * Read a MESSAGE request
 */
public class PublicMessageReader implements Reader<PublicMessagePacket> {

    private final static int BUFFER_SIZE = 1024;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_SERVER}
     * {@link #WAITING_USERNAME}
     * {@link #WAITING_TEXT}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * MESSAGE format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the server name
         */
        WAITING_SERVER,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login username
         */
        WAITING_USERNAME,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the message content
         */
        WAITING_TEXT,
        /**
         * The byte buffer hasn't been filled according to the
         * MESSAGE format
         */
        ERROR
    }

    private State state = State.WAITING_SERVER;
    private final StringReader stringReader = new StringReader();
    private PublicMessagePacket message;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     MESSAGE format
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

        String server = null;
        String username = null;
        String text = null;

        if (state == State.WAITING_SERVER){
            server = readString(buffer, State.WAITING_USERNAME);
        }
        if (state == State.WAITING_USERNAME) {
            username = readString(buffer, State.WAITING_TEXT);
        }
        if (state == State.WAITING_TEXT) {
            text = readString(buffer, State.DONE);
        } else {
            return REFILL;
        }

        if (text == null || username == null) {
            return ERROR;
        }

        message = new PublicMessagePacket(server, username, text);

        state = State.DONE;

        return DONE;
    }

    /**
     * Read a string from the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the MESSAGE format
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to MESSAGE format
     * @return          the string read in the byte buffer
     */
    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            //stringReader.reset();
            message = null;
            stringReader.reset();
            state = nextState;
            return value.message();
        }
        else return null;
    }

    /**
     * Get the value of the MESSAGE request
     * @return the MESSAGE request
     */
    public PublicMessagePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    /**
     * Reset PublicMessageReader's fields
     */
    public void reset() {
        state = State.WAITING_USERNAME;
        stringReader.reset();
        message = null;
    }
}
