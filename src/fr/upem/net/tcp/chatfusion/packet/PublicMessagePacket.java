package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * MESSAGE(4) =
 * 4 (OPCODE)
 * server (STRING<=100)
 * login (STRING<=30)
 * msg (STRING<=1024)
 */

/**
 * <p>
 *     Create a MESSAGE packet.
 *</p>
 * <p>
 *     Create this packet in order to send a text message
 *     from a client to all the clients who are connected
 *     to the mega-server.
 * </p>
 */
public class PublicMessagePacket implements Packet {

    /**
     * Get the source server name
     * @return the source server name
     */
    public String serverSource() {
        return serverSource;
    }

    /**
     * Get the source login username
     * @return the source login username
     */
    public String loginSource() {
        return loginSource;
    }

    /**
     * Get the message content
     * @return the message content
     */
    public String message() {
        return message;
    }

    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String message;

    /**
     * Constructs a MESSAGE packet according to
     * MESSAGE format
     * @param serverSource  the source server name
     * @param loginSource   the source login username
     * @param message       the text message content
     */
    public PublicMessagePacket(
            String serverSource,
            String loginSource,
            String message) {
        this.opcode = OPCODE.MESSAGE_PUBLIC;
        this.serverSource = Packet.verifySize(serverSource, 100);
        this.loginSource = Packet.verifySize(loginSource, 30);
        this.message = Packet.verifySize(message, 1024);
    }

    /**
     * Create a byte buffer according to MESSAGE format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {

        return new Buffer.Builder(opcode)
                .addString(serverSource)
                .addString(loginSource)
                .addString(message)
                .build();
    }

    /**
     * Create a string representation of a MESSAGE packet.
     * @return a string
     */
    @Override
    public String toString() {
        return "PublicMessagePacket{" +
                "opcode=" + opcode +
                ", server='" + serverSource + '\'' +
                ", login='" + loginSource + '\'' +
                ", message='" + message + '\'' +
                '}';
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

    // TO DELETE
    public String from() {
        return loginSource;
    }

    public String getMessage() {
        return message;
    }
}
