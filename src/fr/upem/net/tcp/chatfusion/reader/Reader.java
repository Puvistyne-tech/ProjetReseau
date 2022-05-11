package fr.upem.net.tcp.chatfusion.reader;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The Reader interface allows to read all packets that are
 * implemented by the Packet interface.
 * @param <Packets> the packet provided by the get() method
 */
public interface Reader<Packets> {

    /**
     * Byte buffer status
     * {@link #DONE}
     * {@link #REFILL}
     * {@link #ERROR}
     */
    enum ProcessStatus {
        /**
         * The byte buffer has been fully read
         */
        DONE,
        /**
         * The byte buffer hasn't been fully read
         */
        REFILL,
        /**
         * The byte buffer isn't conform to this packet format
         */
        ERROR
    }

    /**
     * <p>
     *     Extract the content of the byte buffer
     * </p>
     * <p>
     *     According to the convention, the byte buffer is in writing mode
     *     previously and afterward the method call
     * </p>
     * @param buffer the byte buffer
     * @return       the byte buffer status
     */
    ProcessStatus process(ByteBuffer buffer);

    /**
     * Get the value of the packet field specified by Packets
     * @return The packet field
     */
    Packets get();

    /**
     * Reset the Reader
     */
    void reset();

//    //TODO must verify
//    /**
//     * to convert a string to InetAddress
//     * @param input
//     * @return
//     */
//    default URL url(String input) {
//        try {
//            var url = new URL(input);
//            return url;
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * <p>
     *     Extract the port from an socket address in order to instantiate
     *     a InetSocketAddress.
     * </p>
     * <p>
     *     This method is defined as default because his implementation won't
     *     change according to the class that implement Reader interface.
     * </p>
     * @param input the socket address
     * @return      An instance of InetSocketAddress
     */
    default InetSocketAddress getAddress(String input) {
//        try {
//            var url = new URL(input);
//            return new InetSocketAddress(url.getHost(), url.getPort());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
        var a = input.split(":");
        var s = a.length;
        var port = a[s - 1];
        var t = new StringBuffer();
        Arrays.stream(a).toList().forEach(t::append);
//        return new InetSocketAddress(t.toString(), Integer.parseInt(port));
        return new InetSocketAddress(Integer.parseInt(port));
    }
}
