package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

public record OpCodePacket(OPCODE opCode) implements Packet {

    /**
     * Create a byte buffer according to OPCODE format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        var buffer = ByteBuffer.allocate(Byte.BYTES);
        //TODO Faut tester
        buffer.put((byte)opCode.ordinal()).flip();
        return buffer;
    }

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    @Override
    public void accept(IPacketVisitor visitor) {
        //no need to implement this here
    }
}
