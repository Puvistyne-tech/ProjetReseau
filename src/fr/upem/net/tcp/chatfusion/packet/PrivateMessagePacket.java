package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * MESSAGE_PRIVATE(5) =
 * 5 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * msg (STRING<=1024)
 */

/**
 * <p>
 *     Create a MESSAGE_PRIVATE packet.
 *</p>
 * <p>
 *     Create this packet in order to send a text message
 *     from a client to another client.
 * </p>
 */
public class PrivateMessagePacket implements Packet {

    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String severDestination;
    private final String loginDestination;
    private final String message;

    /**
     * Constructs a MESSAGE_PRIVATE packet according to
     * MESSAGE_PRIVATE format
     * @param serverSource      the source server name
     * @param loginSource       the source login username
     * @param severDestination  the destination server name
     * @param loginDestination  the destination login username
     * @param message           the text message content
     */
    public PrivateMessagePacket(
            String serverSource,
            String loginSource,
            String severDestination,
            String loginDestination,
            String message) {
        this.opcode = OPCODE.MESSAGE_PRIVATE;
        this.serverSource = Packet.verifySize(serverSource, 100);
        this.loginSource = Packet.verifySize(loginSource, 30);
        this.severDestination = Packet.verifySize(severDestination, 100);
        this.loginDestination = Packet.verifySize(loginDestination, 30);
        this.message = Packet.verifySize(message, 1024);
    }

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
     * Get the destination server name
     * @return the destination server name
     */
    public String severDestination() {
        return severDestination;
    }

    /**
     * Get the destination login username
     * @return the destination login username
     */
    public String loginDestination() {
        return loginDestination;
    }

    /**
     * Get the message content
     * @return the message content
     */
    public String message() {
        return message;
    }

    /**
     * Create a byte buffer according to MESSAGE_PRIVATE format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(serverSource)
                .addString(loginSource)
                .addString(severDestination)
                .addString(loginDestination)
                .addString(message)
                .build();
    }

    /**
     * Create a string representation of a MESSAGE_PRIVATE packet.
     * @return a string
     */
    @Override
    public String toString() {
        return "PrivateMessagePacket{" +
                "opcode=" + opcode +
                ", serverSource='" + serverSource + '\'' +
                ", loginSource='" + loginSource + '\'' +
                ", severDestination='" + severDestination + '\'' +
                ", loginDestination='" + loginDestination + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    // TO DELETE
    public String from() {
        return loginSource;
    }

    public String to() {
        return loginDestination;
    }

    public String getMessage() {
        return message;
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
}
