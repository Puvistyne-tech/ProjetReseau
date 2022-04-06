package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.OpCodePacket;

import java.nio.ByteBuffer;

public class OpCodeReader implements Reader<OpCodePacket> {

    private enum State {
        DONE, WAITING, ERROR
    }

    private State state = State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Byte.BYTES); // write-mode
    private OpCodePacket value;

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
        value = new OpCodePacket(internalBuffer.get());
        return ProcessStatus.DONE;
    }

    @Override
    public OpCodePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}