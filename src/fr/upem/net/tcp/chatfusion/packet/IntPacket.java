package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * Create an INT packet.
 * @param value an integer
 */
public record IntPacket(int value) implements Packet {

    /**
     * Create a byte buffer according to INT format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value).flip();
        return buffer;
    }

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    @Override
    public void accept(IPacketVisitor visitor) {
//        visitor.visit(this);
    }
    @Override
    public void accept(IPacketVisitor visitor) {
//        visitor.visit(this);
    }
}
