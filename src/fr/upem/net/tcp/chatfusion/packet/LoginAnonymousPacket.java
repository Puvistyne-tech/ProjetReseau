package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * <p>
 *     Create a LOGIN_ANONYMOUS packet.
 *</p>
 * <p>
 *     Create this packet in order to login, from a client,
 *     to a server without a password.
 * </p>
 */
public class LoginAnonymousPacket implements Packet {

    private final String login;
    private final OPCODE opcode;


    /**
     * Constructs a LOGIN_ANONYMOUS packet according to
     * LOGIN_ANONYMOUS format
     * @param login the login username
     */
    public LoginAnonymousPacket(String login) {
        this.login = Packet.verifySize(login, 30);
        this.opcode = OPCODE.LOGIN_ANONYMOUS;
    }

    /**
     * Create a byte buffer according to LOGIN_ANONYMOUS format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder()
                .addOpCode(opcode)
                .addString(login)
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

    /**
     * Get the login username
     * @return the login username
     */
    public String getLogin() {
        return this.login;
    }
}
