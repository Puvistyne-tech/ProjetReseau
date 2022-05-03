package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

public record IntPacket(int value) implements Packet {


    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value).flip();
        return buffer;
    }
    @Override
    public void accept(IPacketVisitor visitor) {
//        visitor.visit(this);
    }
}
