package fr.upem.net.tcp.chatfusion.context;

import fr.upem.net.tcp.chatfusion.packet.Packet;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public interface IContext {

    /**
     * Process the content of bufferIn
     * <p>
     * The convention is that bufferIn is in write-mode before the call to process
     * and after the call
     */
    void processIn() throws Exception;

    /**
     * Try to fill bufferOut from the message queue
     */
    void processOut();

    /**
     * Add a message to the message queue, tries to fill bufferOut and updateInterestOps
     *
     * @param packet the received packet
     */
    void queueMessage(Packet packet);

    /**
     * Update the interestOps of the key looking only at values of the boolean
     * closed and of both ByteBuffers.
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * updateInterestOps and after the call. Also it is assumed that process has
     * been be called just before updateInterestOps.
     */
    void updateInterestOps();

    /**
     * Updates the interest
     * @throws IOException if an I/O error occurs
     */
    void doConnect() throws IOException;

    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     *
     * @throws IOException
     */
    void doRead() throws Exception;

    /**
     * Performs the write action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doWrite and after the call
     *
     * @throws IOException
     */
    void doWrite() throws IOException;

    /**
     * Close the client's channel
     */
    void silentlyClose();

    /**
     * Gets the client's socket
     * @return the client's socket
     */
    SelectionKey getKey();

    /**
     * Sets the connection's status
     */
    void close();

    /**
     * Gets if the connection is closed
     * @return the connection's status
     */
    boolean isClosed();

}
