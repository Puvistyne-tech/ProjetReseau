package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;


public class StringReader implements Reader<StringPacket> {

    private final static int BUFFER_SIZE = 1024;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    private enum State {
        DONE, WAITING_SIZE, WAITING_STRING, ERROR
    }

    private State state = State.WAITING_SIZE;
    private final IntReader intReader = new IntReader();
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private StringPacket value;
    //€a€
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        switch (state) {
            case WAITING_SIZE -> {
                readSize(buffer);
                if (state == State.ERROR) {
                    return ProcessStatus.ERROR;
                }
            }

            case WAITING_STRING -> {
                if (buffer.remaining() < internalBuffer.remaining()) {
                    internalBuffer.put(buffer);
                } else {
                    var oldLimit = buffer.limit();
                    buffer.limit(internalBuffer.remaining());   // On diminue la limite du buffer externe à la taille du buffer interne
                    internalBuffer.put(buffer);                 // pour qu'on puisse l'ajouter
                    buffer.limit(oldLimit);                     // Ensuite on remet le buffer à son état original
                }

                if (internalBuffer.hasRemaining()) {
                    return REFILL;
                }
                state = State.DONE;
                internalBuffer.flip();
                value = new StringPacket(CHARSET.decode(internalBuffer).toString());

                return DONE;
            }


        }

    /*
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
        value = new StringPackets(CHARSET.decode(internalBuffer).toString());

        return DONE;

     */
        return null;
    }

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

    @Override
    public StringPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
        intReader.reset();
        value = null;
    }
}