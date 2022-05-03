package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionInitPacket;

import java.nio.ByteBuffer;

//TODO
public class FusionInitReader implements Reader<FusionInitPacket> {
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return null;
    }

    @Override
    public FusionInitPacket get() {
        return null;
    }

    @Override
    public void reset() {

    }
}
