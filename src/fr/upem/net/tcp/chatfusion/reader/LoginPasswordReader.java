package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginPasswordPacket;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

public class LoginPasswordReader implements Reader<LoginPasswordPacket> {


    private enum State {
        DONE, WAITING_USERNAME, WAITING_PASSWORD, ERROR
    }

    private State state = State.WAITING_USERNAME;
    private final StringReader stringReader = new StringReader();
    private LoginPasswordPacket message;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {

        String login = null;
        String password;

        if (state == State.WAITING_USERNAME) {
            login = readString(buffer, State.WAITING_PASSWORD);
        }
        if (state == State.WAITING_PASSWORD) {
            password = readString(buffer, State.DONE);
        } else {
            return REFILL;
        }

        if (login == null || password == null) {
            return ERROR;
        }

        message = new LoginPasswordPacket(login, password);

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
        } else return null;
    }

    @Override
    public LoginPasswordPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    @Override
    public void reset() {
        state = State.WAITING_USERNAME;
        stringReader.reset();
        message = null;
    }
}
