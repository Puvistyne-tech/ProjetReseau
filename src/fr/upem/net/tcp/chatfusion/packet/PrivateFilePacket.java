package fr.upem.net.tcp.chatfusion.packet;


import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * FILE_PRIVATE(6) =
 * 6 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * filename (STRING<=100)
 * nb_blocks (INT)
 * block_size (INT)
 * block (BYTES)
 */

/**
 * <p>
 *     Create a FILE_PRIVATE packet.
 *</p>
 * <p>
 *     Create this packet in order to send a file from a client
 *     to another client.
 * </p>
 */
public class PrivateFilePacket implements Packet {
    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String serverDestination;
    private final String loginDestination;
    private final String filename;
    private final int nbBlocks;
    private final int blockSize;
    private final ByteBuffer bytes;


    /**
     * Get the current block content
     * @return the content
     */
    public ByteBuffer getBytes() {
        return bytes;
    }

    /**
     * Get the source server name
     * @return the source server name
     */
    public String getServerSource() {
        return serverSource;
    }

    /**
     * Get the source login username
     * @return the source login username
     */
    public String getLoginSource() {
        return loginSource;
    }

    /**
     * Get the destination server name
     * @return the destination server name
     */
    public String getServerDestination() {
        return serverDestination;
    }

    /**
     * Get the destination login username
     * @return the destination login username
     */
    public String getLoginDestination() {
        return loginDestination;
    }

    /**
     * Get the file path
     * @return the file path
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get the number of blocks where the
     * file has been fragmented
     * @return the number of blocks
     */
    public int getNbBlocks() {
        return nbBlocks;
    }

    /**
     * Get the size of the current block
     * @return the current block size
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Constructs a FILE_PRIVATE packet according to
     * FILE_PRIVATE format
     * @param serverSource      the source server name
     * @param loginSource       the source login username
     * @param serverDestination the destination server name
     * @param loginDestination  the destination login username
     * @param filename          the file path
     * @param nbBlocks          the number of blocks where the
     *                          file has been fragmented
     * @param blockSize         the size of the current block
     * @param bytes             the current block content
     */
    public PrivateFilePacket(
            String serverSource,
            String loginSource,
            String serverDestination,
            String loginDestination,
            String filename, int nbBlocks, int blockSize,
            ByteBuffer bytes) {
        this.opcode = OPCODE.FILE_PRIVATE;
        this.serverSource = Packet.verifySize(serverSource, 100);
        this.loginSource = Packet.verifySize(loginSource, 30);
        this.serverDestination = Packet.verifySize(serverDestination, 100);
        this.loginDestination = Packet.verifySize(loginDestination, 30);
        this.filename = Packet.verifySize(filename, 100);
        this.nbBlocks = nbBlocks;
        this.blockSize = blockSize;
        this.bytes = bytes;
    }

    /**
     * Create a byte buffer according to FILE_PRIVATE format
     * @return the byte buffer
     */
    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder()
                .addOpCode(opcode)
                .addString(serverSource)
                .addString(loginSource)
                .addString(serverDestination)
                .addString(loginDestination)
                .addString(filename)
                .addInt(nbBlocks)
                .addInt(blockSize)
                .addBuffer(bytes)
                .build();
    }

    /**
     * Create a string representation of a FILE_PRIVATE packet.
     * @return a string
     */
    @Override
    public String toString() {
        return "PrivateFilePacket{" +
                "opcode=" + opcode +
                ", serverSource='" + serverSource + '\'' +
                ", loginSource='" + loginSource + '\'' +
                ", severDestination='" + serverDestination + '\'' +
                ", loginDestination='" + loginDestination + '\'' +
                ", filename='" + filename + '\'' +
                ", nbBlocks=" + nbBlocks +
                ", blockSize=" + blockSize +
                ", bytes=" + bytes +
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
}
