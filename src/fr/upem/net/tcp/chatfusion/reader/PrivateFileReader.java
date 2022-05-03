package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.PrivateFilePacket;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;

/**
 * FILE_PRIVATE(6) = 6 (OPCODE)
 * server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * filename (STRING<=100)
 * nb_blocks (INT)
 * block_size (INT)
 * block (BYTES)
 */

//TODO entirely
public class PrivateFileReader implements Reader<PrivateFilePacket> {

    private enum State {
        DONE,
        WAITING_SERVER_SRC,
        WAITING_LOGIN_SRC,
        WAITING_SERVER_DEST,
        WAITING_LOGIN_DEST,
        WAITING_FILENAME,
        WAITING_NB_BLOCKS,
        WAITING_BLOCK_SIZE,
        WAITING_BLOCK,
        ERROR
    }

    private State state = State.WAITING_SERVER_SRC;
    private final StringReader stringReader = new StringReader();
    private final IntReader intReader = new IntReader();
    private PrivateFilePacket file;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        String serverSrc = null;
        String loginSrc = null;
        String serverDest = null;
        String loginDest = null;
        String filename = null;
        int nbBlocks = 0;
        int blockSize = 0;
        ByteBuffer block;

        if (state == State.WAITING_SERVER_SRC) {
            serverSrc = readString(buffer, State.WAITING_LOGIN_SRC);
        }
        if (state == State.WAITING_LOGIN_SRC) {
            loginSrc = readString(buffer, State.WAITING_SERVER_DEST);
        }
        if (state == State.WAITING_SERVER_DEST) {
            serverDest = readString(buffer, State.WAITING_LOGIN_DEST);
        }
        if (state == State.WAITING_LOGIN_DEST) {
            loginDest = readString(buffer, State.WAITING_FILENAME);
        }
        if (state == State.WAITING_FILENAME) {
            filename = readString(buffer, State.WAITING_NB_BLOCKS);
        }
        if (state == State.WAITING_NB_BLOCKS) {
            nbBlocks = readInteger(buffer, State.WAITING_BLOCK_SIZE);
        }
        if (state == State.WAITING_BLOCK_SIZE) {
            blockSize = readInteger(buffer, State.WAITING_BLOCK);
        }
        if (state == State.WAITING_BLOCK && blockSize > 0) {
            var lim = buffer.limit();
            buffer.limit(buffer.position() + blockSize);
            block = ByteBuffer.allocate(blockSize);
            block.put(buffer).flip();
            buffer.limit(lim);
            state = State.DONE;
        } else {
            return REFILL;
        }

        if (block.capacity() == 0 ||
                serverDest == null ||
                loginDest == null ||
                serverSrc == null ||
                loginSrc == null ||
                filename == null ||
                nbBlocks == 0 ||
                blockSize == 0
        ) {
            return ERROR;
        }

        this.file = new PrivateFilePacket(
                serverDest,
                loginDest,
                serverDest,
                loginDest,
                filename,
                nbBlocks,
                blockSize,
                block
        );

        state = State.DONE;

        return DONE;
    }

    private int readInteger(ByteBuffer buffer, State nextState) {
        var status = intReader.process(buffer);
        if (status == DONE) {
            var value = intReader.get();
            intReader.reset();
            state = nextState;
            return value.value();
        } else return 0;
    }

    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            file = null;
            stringReader.reset();
            state = nextState;
            return value.message();
        } else return null;
    }

    public PrivateFilePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return file;
    }

    public void reset() {
        state = State.WAITING_SERVER_SRC;
        stringReader.reset();
        intReader.reset();
        file = null;
    }
}
