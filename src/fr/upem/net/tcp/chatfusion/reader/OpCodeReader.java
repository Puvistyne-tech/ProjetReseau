package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import java.nio.ByteBuffer;

public class OpCodeReader implements Reader<OPCODE> {

    private enum State {
        DONE, WAITING, ERROR
    }

    private State state = State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Byte.BYTES); // write-mode
//    private OpCodePacket value;
    private OPCODE value;

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
//        var op=internalBuffer.get();
//        var t= OPCODE.values()[op];
//        value = new OpCodePacket(OPCODE.byteToOpcode(internalBuffer.get()));
        value = OPCODE.byteToOpcode(internalBuffer.get());
        return ProcessStatus.DONE;
    }

    @Override
    public OPCODE get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        value = null;
        internalBuffer.clear();
    }
}