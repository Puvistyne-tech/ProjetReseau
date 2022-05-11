package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.exception.SizeNotRespectedException;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Interface which represents the different types of packets exchanged between the clients the servers
 */
public interface Packet {

    /**
     * Create a byte buffer according to this format
     * @return the byte buffer
     */
    ByteBuffer toByteBuffer();

    /**
     * Create a string representation of the object.
     * @return a string
     */
    String toString();

    /**
     * Perform this operation on the given packet according to his
     * actual type
     * @param visitor the packet
     */
    void accept(IPacketVisitor visitor);

    /**
     * Check if the input string size isn't greater than the expected
     * size
     * @param body the input string
     * @param size the size expected
     * @return     the string
     */
    static String verifySize(String body, int size) {
        Objects.requireNonNull(body);
        try {
            if (body.length() > size) {
                throw new SizeNotRespectedException(size);
            } else return body;
        } catch (SizeNotRespectedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
}
