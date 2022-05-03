package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginAcceptedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.DONE;

public class LoginAcceptedReader implements Reader<LoginAcceptedPacket> {

//    private enum State {
//        DONE, WAITING_SERVER_NAME, ERROR
//    }
//
//    private State state = State.WAITING_SERVER_NAME;
    private final StringReader stringReader = new StringReader();
    private StringPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {

        return stringReader.process(buffer);
    }


    @Override
    public LoginAcceptedPacket get() {

        return new LoginAcceptedPacket(stringReader.get().message());
    }

    @Override
    public void reset() {
//        state = State.WAITING_SERVER_NAME;
        stringReader.reset();
        value = null;
    }
}
