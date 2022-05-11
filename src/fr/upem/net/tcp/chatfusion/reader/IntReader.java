package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.IntPacket;

import java.nio.ByteBuffer;

/**
 * Read an integer from a byte buffer.
 */
public class IntReader implements Reader<IntPacket> {

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled with an integer
         */
        DONE,
        /**
         * The byte buffer isn't fully filled yet
         */
        WAITING,
        /**
         * The byte buffer hasn't been filled with an integer
         */
        ERROR
    }

    private State state = State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode
    private IntPacket value;

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
        value = new IntPacket(internalBuffer.getInt());
        return ProcessStatus.DONE;
    }

    /**
     * Get the integer
     * @return the integer
     */
    @Override
    public IntPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Reset IntReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}