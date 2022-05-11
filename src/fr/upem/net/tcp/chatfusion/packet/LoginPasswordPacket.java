package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * LOGIN_PASSWORD(1) =
 * 1 (OPCODE)
 * login (STRING<=30)
 * password (STRING<=30)
 */

/**
 * <p>
 *     Create a LOGIN_PASSWORD packet.
 *</p>
 * <p>
 *     Create this packet in order to login, from a client,
 *     to a server with a password.
 * </p>
 */
public class LoginPasswordPacket implements Packet {
    private final OPCODE opcode;
    private final String login;
    private final String password;

    /**
     * Constructs a LOGIN_PASSWORD packet according to
     * LOGIN_PASSWORD format
     * @param login     the login username
     * @param password  the login password
     */
    public LoginPasswordPacket(String login, String password) {
        this.opcode = OPCODE.LOGIN_PASSWORD;
        this.login = Packet.verifySize(login, 30);
        this.password = Packet.verifySize(password, 30);
    }

    /**
     * Create a byte buffer according to LOGIN_PASSWORD format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(login)
                .addString(password)
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
