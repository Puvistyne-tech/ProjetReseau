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
public class PrivateFilePacket implements Packet {
    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String severDestination;
    private final String loginDestination;
    private final String filename;
    private final int nbBlocks;
    private final int blockSize;

    public ByteBuffer getBytes() {
        return bytes;
    }

    private final ByteBuffer bytes;

    public String getServerSource() {
        return serverSource;
    }

    public String getLoginSource() {
        return loginSource;
    }

    public String getSeverDestination() {
        return severDestination;
    }

    public String getLoginDestination() {
        return loginDestination;
    }

    public String getFilename() {
        return filename;
    }

    public int getNbBlocks() {
        return nbBlocks;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public PrivateFilePacket(
            String serverSource,
            String loginSource,
            String severDestination,
            String loginDestination,
            String filename, int nbBlocks, int blockSize,
            ByteBuffer bytes) {
        this.opcode = OPCODE.FILE_PRIVATE;
        this.serverSource = Packet.verifySize(serverSource, 100);
        this.loginSource = Packet.verifySize(loginSource, 30);
        this.severDestination = Packet.verifySize(severDestination, 100);
        this.loginDestination = Packet.verifySize(loginDestination, 30);
        this.filename = Packet.verifySize(filename, 100);
        this.nbBlocks = nbBlocks;
        this.blockSize = blockSize;
        this.bytes = bytes;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(serverSource)
                .addString(loginSource)
                .addString(severDestination)
                .addString(loginDestination)
                .addString(filename)
                .addInt(nbBlocks)
                .addInt(blockSize)
                .addBuffer(bytes)
                .build();
    }

    @Override
    public String toString() {
        return "PrivateFilePacket{" +
                "opcode=" + opcode +
                ", serverSource='" + serverSource + '\'' +
                ", loginSource='" + loginSource + '\'' +
                ", severDestination='" + severDestination + '\'' +
                ", loginDestination='" + loginDestination + '\'' +
                ", filename='" + filename + '\'' +
                ", nbBlocks=" + nbBlocks +
                ", blockSize=" + blockSize +
                ", bytes=" + bytes +
                '}';
    }

    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
}
