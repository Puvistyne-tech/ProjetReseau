package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

public record OpCodePacket(OPCODE opCode) implements Packet {


    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Byte.BYTES);
        //TODO Faut tester
        buffer.put((byte)opCode.ordinal()).flip();
        return buffer;
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        //no need to implement this here
    }
}
