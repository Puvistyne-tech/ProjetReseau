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

public class PublicMessageReader implements Reader<PublicMessagePacket> {

    private final static int BUFFER_SIZE = 1024;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    private enum State {
        DONE, WAITING_SERVER, WAITING_USERNAME, WAITING_TEXT, ERROR
    }

    private State state = State.WAITING_SERVER;
    private final StringReader stringReader = new StringReader();
    private PublicMessagePacket message;

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

    public PublicMessagePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    public void reset() {
        state = State.WAITING_USERNAME;
        stringReader.reset();
        message = null;
    }
}
