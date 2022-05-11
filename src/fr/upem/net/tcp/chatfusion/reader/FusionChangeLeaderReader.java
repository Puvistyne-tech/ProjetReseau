package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.FusionChangeLeaderPacket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;
import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.DONE;

/**
 * FUSION_CHANGE_LEADER(14) =
 * 14 (OPCODE)
 * leader_name (STRING)
 * address_leader (SOCKETADDRESS)
 */

/**
 * Read a FUSION_CHANGE_LEADER request
 */
public class FusionChangeLeaderReader implements Reader<FusionChangeLeaderPacket> {
    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #WAITING_SERVER_NAME}
     * {@link #WAITING_ADDRESS}
     * {@link #ERROR}
     */
    private enum State {
        /**
         * The byte buffer has been fully filled according to the
         * FUSION_CHANGE_LEADER format
         */
        DONE,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the server name
         */
        WAITING_SERVER_NAME,
        /**
         * The byte buffer is expecting to receive a string which
         * represent the server address
         */
        WAITING_ADDRESS,
        /**
         * The byte buffer hasn't been filled according to the
         * FUSION_CHANGE_LEADER format
         */
        ERROR
    }

    private State state = State.WAITING_SERVER_NAME;
    private final StringReader stringReader = new StringReader();
    private FusionChangeLeaderPacket packet;

    /**
     * <p>
     *     Extract the content of the byte buffer according to the
     *     FUSION_CHANGE_LEADER format
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

        String serverName = null;
        String address=null;

        if (state == State.WAITING_SERVER_NAME) {
            serverName = readString(buffer, State.WAITING_ADDRESS);
        }
        if (state == State.WAITING_ADDRESS) {
            address = readString(buffer, State.DONE);
        } else {
            return REFILL;
        }

        if (serverName == null || address == null) {
            return ERROR;
        }

        packet = new FusionChangeLeaderPacket(serverName,
//                new InetSocketAddress(
//                url(address).getHost(), url(address).getPort()
                getAddress(address)
//        )
        );

        state = State.DONE;

        return DONE;
    }

    /**
     * Read the content of the byte buffer in order to
     * @param buffer    the byte buffer
     * @param nextState the next field expected to be read
     *                  according to FUSION_CHANGE_LEADER format
     * @return          the string read in the byte buffer
     */
    private String readString(ByteBuffer buffer, State nextState) {
        var status = stringReader.process(buffer);
        if (status == DONE) {
            var value = stringReader.get();
            stringReader.reset();
            state = nextState;
            return value.message();
        } else return null;
    }

    /**
     * Get the value of the FUSION_CHANGE_LEADER request
     * @return the FUSION_CHANGE_LEADER request
     */
    @Override
    public FusionChangeLeaderPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return packet;
    }

    /**
     * Reset the FusionChangeLeaderReader's fields
     */
    @Override
    public void reset() {
        state = State.WAITING_SERVER_NAME;
        packet = null;
    }
}
