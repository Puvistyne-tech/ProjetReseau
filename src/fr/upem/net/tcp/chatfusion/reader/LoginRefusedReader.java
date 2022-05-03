package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.LoginAcceptedPacket;
import fr.upem.net.tcp.chatfusion.packet.LoginRefusedPacket;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;

public class LoginRefusedReader implements Reader<LoginRefusedPacket> {

    private enum State {
        DONE, ERROR
    }

    private final StringReader stringReader = new StringReader();
    private StringPacket value;
    private State state = State.DONE;


    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return ProcessStatus.DONE;
    }

    @Override
    public LoginRefusedPacket get() {
        return new LoginRefusedPacket();

    }

    @Override
    public void reset() {
        value = null;
    }
}
