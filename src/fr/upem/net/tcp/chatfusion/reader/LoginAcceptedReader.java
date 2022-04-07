package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginAcceptedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;

public class LoginAcceptedReader implements Reader<LoginAcceptedPacket> {

    private enum State {
        DONE, WAITING_SIZE, WAITING_STRING, ERROR
    }

    private State state = State.WAITING_SIZE;
    private final IntReader intReader = new IntReader();
    private StringPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return null;
    }

    @Override
    public LoginAcceptedPacket get() {
        return null;
    }

    @Override
    public void reset() {

    }
}
