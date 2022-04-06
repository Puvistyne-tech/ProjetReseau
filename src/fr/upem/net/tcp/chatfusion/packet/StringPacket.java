package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;

import java.nio.ByteBuffer;

public record StringPacket(String message) implements Packet {

    @Override
    public ByteBuffer toByteBuffer() {

//        var tmp = CHARSET.encode(message);
//        var intBuffer = new IntPacket(tmp.capacity()).toByteBuffer();
//        var buffer = ByteBuffer.allocate(tmp.capacity() + Integer.BYTES);
//        buffer.put(intBuffer).put(tmp);
//        buffer.flip();

        var buff = new Buffer.Builder()
                .addString(CHARSET.encode(message)).build();

        return buff;
    }
}
