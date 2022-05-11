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

/**
 * Read a FILE_PRIVATE request
 */
public class PrivateFileReader implements Reader<PrivateFilePacket> {

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_SERVER_SRC}
     * {@link #WAITING_LOGIN_SRC}
     * {@link #WAITING_SERVER_DEST}
     * {@link #WAITING_FILENAME}
     * {@link #WAITING_NB_BLOCKS}
     * {@link #WAITING_BLOCK_SIZE}
     * {@link #WAITING_BLOCK}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * FILE_PRIVATE format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the source server name
         */
        WAITING_SERVER_SRC,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login source username
         */
        WAITING_LOGIN_SRC,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the destination server name
         */
        WAITING_SERVER_DEST,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the login destination username
         */
        WAITING_LOGIN_DEST,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the file path
         */
        WAITING_FILENAME,
        /**
         * The byte buffer is expecting to receive an integer which
         * represent the number of blocks where the file has been fragmented
         */
        WAITING_NB_BLOCKS,
        /**
         * The byte buffer is expecting to receive an integer which
         * represent the size of the current block
         */
        WAITING_BLOCK_SIZE,
        /**
         * The byte buffer is expecting to receive one or several bytes which
         * represent the current block content
         */
        WAITING_BLOCK,
        /**
         * The byte buffer hasn't been filled according to the
         * FILE_PRIVATE format
         */
        ERROR
    }

    private State state = State.WAITING_SERVER_SRC;
    private final StringReader stringReader = new StringReader();
    private final IntReader intReader = new IntReader();

    private PrivateFilePacket file;

    /**
     * <p>
     * Extract the content of the byte buffer according to the
     * FILE_PRIVATE format
     * </p>
     * <p>
     * According to the convention, the byte buffer is in writing mode
     * previously and afterward the method call
     * </p>
     *
     * @param buffer the byte buffer
     * @return the byte buffer status
     */
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
        ByteBuffer block = null;

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
                block = ByteBuffer.allocate(blockSize);
                buffer.flip();
            try {
                if (buffer.remaining() <= block.remaining()) { // Si le buffer interne a assez de place pour contenir tout le buffer externe
                    block.put(buffer);
                } else {
                    var oldLimit = buffer.limit();
                    buffer.limit(block.remaining()); // On diminue la limite du buffer externe à la taille du buffer interne
                    block.put(buffer);                 // pour qu'on puisse l'ajouter
                    buffer.limit(oldLimit);                     //Ensuite on remet le buffer à son état original
                }
            } finally {
                buffer.compact(); //On remet le buffer à son état original (pos = 0, limit = 1024) //
            }

            if (block.hasRemaining()) {
                return REFILL;
            }

            state = State.DONE;
        } else {
            return REFILL;
        }

        if (block == null ||
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
                ByteBuffer.allocate(blockSize).put(block)
        );

        state = State.DONE;

        return DONE;
    }

    /**
     * Read an integer from the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the FILE_PRIVATE format
     *
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to FILE_PRIVATE format
     * @return the integer read in the byte buffer
     */
    private int readInteger(ByteBuffer buffer, State nextState) {
        var status = intReader.process(buffer);
        if (status == DONE) {
            var value = intReader.get();
            intReader.reset();
            state = nextState;
            return value.value();
        } else return 0;
    }

    /**
     * Read a string from the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the FILE_PRIVATE format
     *
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to FILE_PRIVATE format
     * @return the string read in the byte buffer
     */
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

    /**
     * Get the value of the FILE_PRIVATE request
     *
     * @return the FILE_PRIVATE request
     */
    public PrivateFilePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return file;
    }

    /**
     * Reset PrivateFileReader's fields
     */
    public void reset() {
        state = State.WAITING_SERVER_SRC;
        stringReader.reset();
        intReader.reset();
        file = null;
    }
}
