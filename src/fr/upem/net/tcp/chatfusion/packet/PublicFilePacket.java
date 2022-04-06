package fr.upem.net.tcp.chatfusion.packet;


import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * FILE_PUBLIC(7) =
 * 7 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * filename (STRING<=100)
 * nb_blocks (INT)
 * block_size (INT)
 * block (BYTES)
 */
public class PublicFilePacket implements Packet {
    private final OPCODE opcode;
    private final String serverSource;
    private final String loginSource;
    private final String severDestination;
    private final String loginDestination;
    private final String filename;
    private final int nbBlocks;
    private final int blockSize;
    private final Byte bytes;

    public PublicFilePacket(
                            String serverSource,
                            String loginSource,
                            String severDestination,
                            String loginDestination,
                            String filename, int nbBlocks, int blockSize,
                            Byte bytes)  {
        this.opcode = OPCODE.FILE_PUBLIC;
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
                .addString(new StringPacket(serverSource).toByteBuffer())
                .addString(new StringPacket(loginSource).toByteBuffer())
                .addString(new StringPacket(severDestination).toByteBuffer())
                .addString(new StringPacket(loginDestination).toByteBuffer())
                .addString(new StringPacket(filename).toByteBuffer())
                .addInt(nbBlocks)
                .addInt(blockSize)
                .addBytes(bytes)
                .build();
    }
}
