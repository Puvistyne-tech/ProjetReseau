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

public class PrivateMessageReader implements Reader<PrivateMessagePacket> {

    private enum State {
        DONE,
        WAITING_SERVER_SRC,
        WAITING_USERNAME_SRC,
        WAITING_SERVER_DEST,
        WAITING_USERNAME_DEST,
        WAITING_TEXT,
        ERROR
    }

    private State state = State.WAITING_SERVER_SRC;
    private final StringReader stringReader = new StringReader();
    private PrivateMessagePacket message;

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
    @Override
    public PrivateMessagePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    @Override
    public void reset() {
        state = State.WAITING_SERVER_SRC;
        stringReader.reset();
        message = null;
    }
}
