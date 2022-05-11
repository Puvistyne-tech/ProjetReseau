package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.Packet;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;
import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.ERROR;

/**
 * Read a packet
 */
public class PacketReader implements Reader<Packet> {
    private Reader<? extends Packet> reader;

    /**
     * <p>
     *     Instantiate and process the byte buffer with the proper reader according to
     *     the OPCODE contained in the byte buffer
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
        OpCodeReader opCodeReader = new OpCodeReader();
        var opCode = opCodeReader.process(buffer);
        if (opCode == DONE) {
            var op = opCodeReader.get();
            switch (op) {
                case LOGIN_ANONYMOUS -> reader = new LoginAnonymousReader();
                case LOGIN_PASSWORD -> reader = new LoginPasswordReader();
                case LOGIN_ACCEPTED -> reader = new LoginAcceptedReader();
                case LOGIN_REFUSED -> reader = new LoginRefusedReader();
                case MESSAGE_PUBLIC -> reader = new PublicMessageReader();
                case MESSAGE_PRIVATE -> reader = new PrivateMessageReader();
                case FILE_PRIVATE -> reader = new PrivateFileReader();
                case FUSION_INIT -> reader = new FusionInitReader();
                case FUSION_INIT_OK -> reader = new FusionInitOKReader();
                case FUSION_INIT_KO -> reader = new FusionInitKOReader();
                case FUSION_INIT_FWD -> reader = new FusionInitFWDReader();
                case FUSION_REQUEST -> reader = new FusionRequestReader();
                case FUSION_REQUEST_RESP -> reader = new FusionRequestResReader();
                case FUSION_CHANGE_LEADER -> reader = new FusionChangeLeaderReader();
                case FUSION_MERGE -> reader = new FusionMergeReader();
                case ERROR -> {
                    return ERROR;
                }
                default -> {
                    return REFILL;
                }
            }
        }

        return reader.process(buffer);
    }

    /**
     * Get the value of the packet
     * @return the packet
     */
    @Override
    public Packet get() {
        if (reader == null) return null;
        return reader.get();
    }

    /**
     * Reset the PacketReader's field
     */
    @Override
    public void reset() {
        //TODO
        // if(reader!=null)
        reader.reset();
    }

}
