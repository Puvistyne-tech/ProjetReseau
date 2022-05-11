package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

/**
 * Read an OPCODE
 */
public class OpCodeReader implements Reader<OPCODE> {

    private final ByteReader byteReader = new ByteReader();

    /**
     * <p>
     *      Extract the byte of the byte buffer which represent the OPCODE
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
        return byteReader.process(buffer);
    }

    /**
     * Get the value of the OPCODE
     * @return the OPCODE
     */
    @Override
    public OPCODE get() {
        return OPCODE.byteToOpcode(byteReader.get());
    }

    /**
     * Reset the OpCodeReader's field
     */
    @Override
    public void reset() {
        byteReader.reset();
    }

}