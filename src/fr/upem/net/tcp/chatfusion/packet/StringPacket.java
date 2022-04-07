package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;

import java.nio.ByteBuffer;

public record StringPacket(String message) implements Packet {

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder()
                .addString(message).build();
    }

    @Override
    public String message() {
        return message;
    }
}
