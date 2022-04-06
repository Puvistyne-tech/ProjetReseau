package fr.upem.net.tcp.chatfusion.packet;

import java.nio.ByteBuffer;

public record OpCodePacket(Byte value) implements Packet {


    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put(value).flip();
        return buffer;
    }
}
