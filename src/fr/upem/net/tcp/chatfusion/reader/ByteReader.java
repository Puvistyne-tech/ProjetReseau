package fr.upem.net.tcp.chatfusion.reader;

import java.nio.ByteBuffer;

/**
 * Read a Byte from
 * a byte buffer.
 */
public class ByteReader implements Reader<Byte> {
    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled with a Byte
         */
        DONE,
        /**
         * The byte buffer isn't fully filled yet
         */
        WAITING,
        /**
         * The byte buffer hasn't been filled with a Byte
         */
        ERROR
    }

    private State state = State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Byte.BYTES); // write-mode
    private Byte value;

    /**
     * Status of the byte buffer
     * @param buffer the byte buffer
     * @return       the byte buffer status
     */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {

        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        buffer.flip();

        try {
            if (buffer.remaining() <= internalBuffer.remaining()) {
                internalBuffer.put(buffer);
            } else {
                var oldLimit = buffer.limit();
                buffer.limit(internalBuffer.remaining());
                internalBuffer.put(buffer);
                buffer.limit(oldLimit);
            }
        } finally {
            buffer.compact();
        }

        if (internalBuffer.hasRemaining()) {
            return ProcessStatus.REFILL;
        }

        state = State.DONE;
        internalBuffer.flip();

        value = internalBuffer.get();
        return ProcessStatus.DONE;
    }

    /**
     * Get the Byte
     * @return The byte
     */
    @Override
    public Byte get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Reset ByteReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING;
        value = null;
        internalBuffer.clear();
    }

}