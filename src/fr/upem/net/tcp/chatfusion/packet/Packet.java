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
    Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * The method to convert the packet to a byte buffer
     *
     * @return returns a Bytebuffer contains the data related to its packet
     */
    ByteBuffer toByteBuffer();

//    void accept(IPacketVisitor visitor);

    String toString();

    void accept(IPacketVisitor visitor);

    /**
     * Send the size of the buffer
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
