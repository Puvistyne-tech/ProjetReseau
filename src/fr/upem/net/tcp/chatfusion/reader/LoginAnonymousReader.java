package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.StringPacket;

import java.nio.ByteBuffer;

public class LoginAnonymousReader implements Reader<StringPacket> {

    private final StringReader stringReader = new StringReader();

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return stringReader.process(buffer);
    }

    @Override
    public StringPacket get() {
        return stringReader.get();
    }

    @Override
    public void reset() {
        stringReader.reset();
    }
}
