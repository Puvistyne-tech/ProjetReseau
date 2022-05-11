package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;


/**
 * MESSAGE_PRIVATE(5) =
 * 5 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * msg (STRING<=1024)
 */

/**
 * Read a MESSAGE_PRIVATE request
 */
public class PrivateMessageReader implements Reader<PrivateMessagePacket> {

    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * MESSAGE_PRIVATE format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the source server name
         */
        WAITING_SERVER_SRC,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login source username
         */
        WAITING_USERNAME_SRC,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the destination server name
         */
        WAITING_SERVER_DEST,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the destination login username
         */
        WAITING_USERNAME_DEST,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the content of the message
         */
        WAITING_TEXT,
        /**
         * The byte buffer hasn't been filled according to the
         * MESSAGE_PRIVATE format
         */
        ERROR
    }

    private State state = State.WAITING_SERVER_SRC;
    private final StringReader stringReader = new StringReader();
    private PrivateMessagePacket message;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     MESSAGE_PRIVATE format
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

        if (state==State.DONE||state==State.ERROR){
            throw new IllegalStateException();
        }

        String serverSrc = null, loginSrc = null, serverDest = null, loginDest = null , textMessage = null;

        if (state == State.WAITING_SERVER_SRC){
            serverSrc = readString(buffer, State.WAITING_USERNAME_SRC);
        }
        if (state == State.WAITING_USERNAME_SRC) {
            loginSrc = readString(buffer, State.WAITING_SERVER_DEST);
        }
        if (state == State.WAITING_SERVER_DEST) {
            serverDest = readString(buffer, State.WAITING_USERNAME_DEST);
        }
        if (state == State.WAITING_USERNAME_DEST) {
            loginDest = readString(buffer, State.WAITING_TEXT);
        }
        if (state == State.WAITING_TEXT) {
            textMessage = readString(buffer, State.DONE);
        }
        else {
            return REFILL;
        }

        if (textMessage == null || loginSrc == null || loginDest == null || serverSrc == null || serverDest == null) {
            return ERROR;
        }

        message = new PrivateMessagePacket(serverSrc, loginSrc, serverDest, loginDest, textMessage);

        state = State.DONE;

        return DONE;
    }
    /**
     * Read a string from the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the MESSAGE_PRIVATE format
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to MESSAGE_PRIVATE format
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
     * Get the value of the MESSAGE_PRIVATE request
     * @return the MESSAGE_PRIVATE request
     */
    @Override
    public PrivateMessagePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    /**
     * Reset PrivateMessageReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING_SERVER_SRC;
        stringReader.reset();
        message = null;
    }
}
