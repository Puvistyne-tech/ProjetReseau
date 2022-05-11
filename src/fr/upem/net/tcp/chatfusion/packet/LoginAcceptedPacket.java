package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * LOGIN_ACCEPTED(2) = 2 (OPCODE)
 * serveur name (STRING<=100)
 */

/**
 * <p>
 *     Create a LOGIN_ACCEPTED packet.
 *</p>
 * <p>
 *     Create this packet in order to specify to the
 *     remote server that the login has been successful.
 * </p>
 */
public class LoginAcceptedPacket implements Packet {
    private final OPCODE opcode;

    public String server() {
        return server;
    }

    private final String server;

    /**
     * Constructs a LOGIN_ACCEPTED packet according to
     * LOGIN_ACCEPTED format
     * @param server the remote server
     */
    public LoginAcceptedPacket(String server) {
        opcode = OPCODE.LOGIN_ACCEPTED;
        this.server = Packet.verifySize(server, 100);
    }

    /**
     * Get the server name
     * @return the server name
     */
    public String server() {
        return server;
    }

    /**
     * Create a byte buffer according to LOGIN_ACCEPTED format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(server)
                .build();
    }

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
