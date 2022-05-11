package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

/**
 * Read a string
 */
public class StringReader implements Reader<StringPacket> {

    private final static int BUFFER_SIZE = 1024;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_SIZE}
     * {@link #WAITING_STRING}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled with a string having
         * the specified size (cf., WAITING_SIZE)
         */
        DONE,
        /**
         * The byte buffer is expecting to receive an integer which
         * represent the expected string size
         */
        WAITING_SIZE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the actual string
         */
        WAITING_STRING,
        /**
         * The byte buffer hasn't been filled with a valid string
         */
        ERROR
    }

    private State state = State.WAITING_SIZE;
    private final IntReader intReader = new IntReader();
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private StringPacket value;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     STRING format
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

        if (state == State.WAITING_SIZE) {
            readSize(buffer);
            if (state == State.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if (state != State.WAITING_STRING) {
            return REFILL;
        }

        buffer.flip();

        try {
            if (buffer.remaining() <= internalBuffer.remaining()) { // Si le buffer interne a assez de place pour contenir tout le buffer externe
                internalBuffer.put(buffer);
            } else {
                var oldLimit = buffer.limit();
                buffer.limit(internalBuffer.remaining()); // On diminue la limite du buffer externe à la taille du buffer interne
                internalBuffer.put(buffer);                 // pour qu'on puisse l'ajouter
                buffer.limit(oldLimit);                     //Ensuite on remet le buffer à son état original
            }
        } finally {
            buffer.compact(); //On remet le buffer à son état original (pos = 0, limit = 1024) //
        }

        if (internalBuffer.hasRemaining()) {
            return REFILL;
        }


        state = State.DONE;
        internalBuffer.flip();
        value = new StringPacket(CHARSET.decode(internalBuffer).toString());

        return DONE;

    }

    /**
     * Read an integer from the byte buffer in order to get the string size
     * and change the byte buffer status according to the STRING format
     * @param buffer the byte buffer
     * @return       the integer read in the byte buffer
     */
    private void readSize(ByteBuffer buffer) {
        var status = intReader.process(buffer);
        if (status == DONE) {
            var sizeToRead = intReader.get().value();
            if (sizeToRead > BUFFER_SIZE || sizeToRead < 0) {
                state = State.ERROR;
                return;
            }
            internalBuffer.limit(sizeToRead);
            intReader.reset();
            state = State.WAITING_STRING;
        }
    }

    /**
     * Get the value of the STRING
     * @return the STRING
     */
    @Override
    public StringPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Reset StringReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
        intReader.reset();
        value = null;
    }
}