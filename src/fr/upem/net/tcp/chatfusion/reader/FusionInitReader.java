package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionInitPacket;
import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.DONE;
import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.ERROR;
import static java.nio.file.Files.readString;

/**
 * FUSION_INIT(8) =
 * 8 (OPCODE)
 * name (STRING<=100)
 * adresse (SOCKETADDRESS)
 * nb_members (INT)
 * name_0 (STRING<=100)
 * name_1 ...
 */

/**
 * Read a FUSION_INIT request
 */
public class FusionInitReader implements Reader<FusionInitPacket> {
    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_SERVER_SRC}
     * {@link #WAITING_ADDRESS}
     * {@link #WAITING_NB_MEMBERS}
     * {@link #WAITING_MEMBERS}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * FUSION_INIT format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the server name
         */
        WAITING_SERVER_SRC,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the server address
         */
        WAITING_ADDRESS,
        /**
         * The byte buffer is expecting to receive an integer which
         * represent the number of other members from this mega-server
         */
        WAITING_NB_MEMBERS,
        /**
         * The byte buffer is expecting to receive one or
         * several strings which represent each members specified in
         * the previous buffer state (i.e., WAITING_NB_MEMBERS).
         */
        WAITING_MEMBERS,
        /**
         * The byte buffer hasn't been filled according to the
         * FUSION_INIT format
         */
        ERROR
    }

    private State state = State.WAITING_SERVER_SRC;
    private final StringReader stringReader = new StringReader();
    private final IntReader intReader = new IntReader();
    private FusionInitPacket packet;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_INIT format
     * </p>
     * <p>
     *     According to the convention, the byte buffer is in writing mode
     *     previously and afterward the method call
     * </p>
     * @param buffer the byte buffer
     * @return       the byte buffer status
     */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {

        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        String serverSrc = null;
        String address = null;
        int nbMembers = 0;
        List<String> members = new ArrayList<>();

        if (state == State.WAITING_SERVER_SRC) {
            serverSrc = readString(buffer, State.WAITING_ADDRESS);
        }
        if (state == State.WAITING_ADDRESS) {
            address = readString(buffer, State.WAITING_NB_MEMBERS);
        }
        if (state == State.WAITING_NB_MEMBERS) {
            var t = intReader.process(buffer);
            if (t == DONE) {
                nbMembers = intReader.get().value();
                intReader.reset();
                state = State.WAITING_MEMBERS;
            }
        }
        for (int i = 0; i < nbMembers; i++) {
            members.add(readString(buffer, State.WAITING_MEMBERS));
        }
        if (state == State.WAITING_MEMBERS && members.size() == nbMembers) {
            state = State.DONE;
        } else {
            return ERROR;
        }

        if (serverSrc == null ||
                address == null ||
                members.size() != nbMembers
        ) {
            return ERROR;
        }

        var addr = address.split(":")[0];
        var port = address.split(":")[1];
        packet = new FusionInitPacket(serverSrc, new InetSocketAddress(Integer.parseInt(port)), members);

        state = State.DONE;

        return DONE;
    }

    /**
     * Read the content of the byte buffer in order to get the proper
     * number of bytes and change the byte buffer status according to
     * the FUSION_INIT format
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to FUSION_INIT format
     * @return          the string read in the byte buffer
     */
    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            packet = null;
            stringReader.reset();
            state = nextState;
            return value.message();
        } else return null;
    }

    /**
     * Get the value of the FUSION_INIT request
     * @return the FUSION_INIT request
     */
    @Override
    public FusionInitPacket get() {
        return packet;
    }

    /**
     * Reset the FusionInitReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING_SERVER_SRC;
        stringReader.reset();
        intReader.reset();
        packet = null;
    }
}
