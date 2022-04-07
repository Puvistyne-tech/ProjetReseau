package fr.upem.net.tcp.chatfusion.buffer;

import fr.upem.net.tcp.chatfusion.exception.BufferSizeExceededException;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Buffer {

    private static int BUFF_SIZE = 1024;

    private ByteBuffer buffer;

    private Buffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    private ByteBuffer getBuffer() {
        return this.buffer.flip();
    }

    public static class Builder {
        private final ByteBuffer byteBuffer;

        public Builder() {
            byteBuffer = ByteBuffer.allocate(BUFF_SIZE);
        }

        public Builder(OPCODE opcode) {
            var b = (byte) opcode.ordinal();
            byteBuffer = ByteBuffer.allocate(BUFF_SIZE);
            byteBuffer.put(b);
        }

        public Builder addOpCode(OPCODE opcode) {
            var b = (byte) opcode.ordinal();
            byteBuffer.put(b);
            return this;
        }

        public Builder addInt(int intValue) {
            byteBuffer.putInt(intValue);
            return this;
        }

        public Builder addBytes(Byte bytes) {
            byteBuffer.put(bytes);
            return this;
        }

        public Builder addBuffer(ByteBuffer newBuffer) {
            newBuffer.flip();
            if (this.byteBuffer.remaining() >= newBuffer.remaining()) {
                byteBuffer.put(newBuffer);
                return this;
            }
            throw new BufferSizeExceededException();
        }

        public Builder addStringPacket(Packet packet) {

            if (packet.toByteBuffer().remaining() <= BUFF_SIZE - 1) {
                this.byteBuffer.put(packet.toByteBuffer());
                return this;
            } else throw new BufferSizeExceededException();
        }

        public Builder addString(String string) {
            var buffer = StandardCharsets.UTF_8.encode(string);

            if (buffer.remaining() <= BUFF_SIZE - 1) {
                this.byteBuffer.putInt(buffer.remaining()).put(buffer);
                return this;
            } else throw new BufferSizeExceededException();
        }

        public ByteBuffer build() {
            return new Buffer(byteBuffer).getBuffer();
        }
    }
}
