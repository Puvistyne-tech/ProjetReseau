package fr.upem.net.tcp.chatfusion.packet;

import java.nio.ByteBuffer;

public record IntPacket(int value) implements Packet {


    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value).flip();
        return buffer;
    }
}
