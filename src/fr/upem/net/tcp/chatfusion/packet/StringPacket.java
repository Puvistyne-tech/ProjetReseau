package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * Create an STRING packet.
 * @param message a text message
 */
public record StringPacket(String message) implements Packet {

    /**
     * Create a byte buffer according to STRING format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder()
                .addString(message).build();
    }

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    @Override
    public void accept(IPacketVisitor visitor) {

    }

    /**
     * Get the message content
     * @return the message content
     */
    @Override
    public String message() {
        return message;
    }

}
