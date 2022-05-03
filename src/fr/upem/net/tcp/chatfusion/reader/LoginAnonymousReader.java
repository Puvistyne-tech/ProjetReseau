package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginAnonymousPacket;


import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

public class LoginAnonymousReader implements Reader<LoginAnonymousPacket> {

    private enum State {
        DONE, WAITING_USERNAME, ERROR
    }

    private State state = State.WAITING_USERNAME;
    private final StringReader stringReader = new StringReader();
    private LoginAnonymousPacket login;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        String string = null;

        if (state == State.WAITING_USERNAME) {
            string = readString(buffer, State.DONE);
        } else {
            return REFILL;
        }

        if (string == null) {
            return ERROR;
        }

        login = new LoginAnonymousPacket(string);

        state = State.DONE;

        return DONE;
    }

    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            //stringReader.reset();
            login = null;
            stringReader.reset();
            state = nextState;
            return value.message();
        } else return null;
    }

    @Override
    public LoginAnonymousPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return login;
    }

    @Override
    public void reset() {
        state = State.WAITING_USERNAME;
        stringReader.reset();
        login = null;
    }

}
