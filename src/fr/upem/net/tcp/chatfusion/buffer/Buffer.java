package fr.upem.net.tcp.chatfusion.buffer;

import fr.upem.net.tcp.chatfusion.exception.BufferSizeExceededException;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *     Create and fill a byte buffer.
 * </p>
 * <p>
 *     This class allow us to manipulate a buffer more easily and
 *     clearly within the code by using invocation chaining.
 *     Although, the class ByteBuffer already implement a builder,
 *     this class has been designed for clean code purposes.
 * </p>
 */
public class Buffer {

    private static final int BUFF_SIZE = 1024;

    private ByteBuffer buffer;

    /**
     * Constructs a byte buffer
     * @param buffer the byte buffer
     */
    private Buffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Get the byte buffer
     * @return the buffer buffer
     */
    private ByteBuffer getBuffer() {
        return this.buffer.flip();
    }

    /**
     * The inner class that allow invocation chaining in order to
     * fill the byte buffer
     */
    public static class Builder {
        private final ByteBuffer byteBuffer;

        /**
         * Allocates a new byte buffer
         */
        public Builder() {
            byteBuffer = ByteBuffer.allocate(BUFF_SIZE);
        }

        /**
         * Builder constructer with a custom buffer size
         * @param bufferSize custom buffer size
         */
        public Builder(int bufferSize){
            byteBuffer=ByteBuffer.allocate(bufferSize);
        }

        /**
         * Constructs a byte buffer sets with an OpCode
         * @param opcode the OpCode
         */
        public Builder(OPCODE opcode) {
            var b = (byte) opcode.ordinal();
            byteBuffer = ByteBuffer.allocate(BUFF_SIZE);
            byteBuffer.put(b);
        }



        /**
         * Fills the byte buffer with an OpCode
         * @param opcode the OpCode
         * @return       this instance of Builder
         */
        public Builder addOpCode(OPCODE opcode) {
            var b = (byte) opcode.ordinal();
            byteBuffer.put(b);
            return this;
        }

        /**
         * Fills the byte buffer with an integer
         * @param intValue the integer
         * @return         this instance of Builder
         */
        public Builder addInt(int intValue) {
            byteBuffer.putInt(intValue);
            return this;
        }

        /**
         * Fills the byte buffer with a byte
         * @param bytes the byte
         * @return      this instance of Builder
         */
        public Builder addBytes(Byte bytes) {
            byteBuffer.put(bytes);
            return this;
        }

        /**
         * Fills the byte buffer with the content of
         * another byte buffer
         * @param newBuffer the byte buffer from where the adding
         *                  content will be taken
         * @return          this instance of Builder
         */
        public Builder addBuffer(ByteBuffer newBuffer) {
            newBuffer.flip();
            if (this.byteBuffer.remaining() >= newBuffer.remaining()) {
                byteBuffer.put(newBuffer);
                return this;
            }
            throw new BufferSizeExceededException();
        }

        /**
         * Fills the byte buffer with the value of a Packet
         * @param packet the Packet from where the adding
         *               content will be taken
         * @return       this instance of Builder
         */
        public Builder addStringPacket(Packet packet) {

            if (packet.toByteBuffer().remaining() <= BUFF_SIZE - 1) {
                this.byteBuffer.put(packet.toByteBuffer());
                return this;
            } else throw new BufferSizeExceededException();
        }

        /**
         * Fills the byte buffer with a string
         * @param string the string
         * @return       this instance of Builder
         */
        public Builder addString(String string) {
            var buffer = StandardCharsets.UTF_8.encode(string);

            if (buffer.remaining() <= BUFF_SIZE - 1) {
                this.byteBuffer.putInt(buffer.remaining()).put(buffer);
                return this;
            } else throw new BufferSizeExceededException();
        }

        /**
         * Gets the filled byte buffer
         * @return this byte buffer
         */
        public ByteBuffer build() {
            return new Buffer(byteBuffer).getBuffer();
        }
    }
}
