package fr.upem.net.tcp.chatfusion.context;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.reader.PacketReader;
import fr.upem.net.tcp.chatfusion.reader.Reader;
import fr.upem.net.tcp.chatfusion.visitor.ClientVisitor;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;
import org.junit.platform.engine.TestDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 *
 */
public abstract class AContext implements IContext {

    private static final Logger logger = Logger.getLogger(AContext.class.getName());
    private static final int BUFFER_SIZE = 1_024;

    private final SelectionKey key;
    private boolean activeSinceLastCheck = true;
    private final SocketChannel socketChannel;
    protected final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(10);
    private boolean closed = false;
    protected IPacketVisitor visitor;

    /**
     * Sets if the client/server status last time it was checked by
     * the manager was active
     *
     * @param activeSinceLastCheck whether if the client/server status last time it was checked by the manager
     */
    public void setActiveSinceLastCheck(boolean activeSinceLastCheck) {
        this.activeSinceLastCheck = activeSinceLastCheck;
    }

    /**
     * Gets the client/server status last time it was checked by
     * the manager
     *
     * @return true if the client/server was active last time it
     * was checked by the manager
     */
    public boolean isActiveSinceLastCheck() {
        return activeSinceLastCheck;
    }

    /**
     * Gets the client/server's socket channel
     *
     * @return the socket channel
     */
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * Constructs a context
     *
     * @param key the client/server's socket
     */
    protected AContext(SelectionKey key) {
        this.key = key;
        this.socketChannel = (SocketChannel) key.channel();
    }

    /**
     * <p>
     * Process the content of bufferIn
     * </p>
     * <p>
     * The convention is that bufferIn is in write-mode before the call to process
     * and after the call
     * </p>
     */
    @Override
    public void processIn() {
        var reader = new PacketReader();
        for (; ; ) {
            assert reader != null;
            Reader.ProcessStatus status = reader.process(bufferIn);
            switch (status) {
                case DONE:
                    var value = reader.get();
                    value.accept(visitor);
                    reader.reset();
                    return;
                case REFILL:
                    return;
                case ERROR:
                    silentlyClose();
                    return;
            }
        }
    }

    /**
     * Tries to fill bufferOut from the message queue
     */
    @Override
    public void processOut() {
        while (!queue.isEmpty() && bufferIn.hasRemaining()) {
            var packet = queue.poll();
            var buff = packet.toByteBuffer();
            if (buff.remaining() <= bufferOut.remaining()) {
                bufferOut.put(buff);
            }
        }
    }

    /**
     * Add a message to the message queue, tries to fill bufferOut and updateInterestOps
     *
     * @param packet the received packet
     */
    @Override
    public void queueMessage(Packet packet) {
        if (!queue.isEmpty()) return;
        if (!queue.offer(packet)) return;
        processOut();
        updateInterestOps();
    }

    /**
     * Gets if the connection is closed
     *
     * @return the connection's status
     */
    @Override
    public boolean isClosed() {
        return closed;
    }


    /**
     * <p>
     * Update the interestOps of the key looking only at values of the boolean
     * closed and of both ByteBuffers.
     * </p>
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * updateInterestOps and after the call. Also it is assumed that process has
     * been be called just before updateInterestOps.
     * </p>
     */
    @Override
    public void updateInterestOps() {
        int interestOps = 0;
        if (!closed && bufferIn.hasRemaining()) {
            interestOps |= SelectionKey.OP_READ;
        }
        if (bufferOut.position() != 0) {
            interestOps |= SelectionKey.OP_WRITE;
        }
        if (interestOps == 0) {
            silentlyClose();
            return;
        } else {
            if (key.isValid()) {
                key.interestOps(interestOps);
            } else {
                silentlyClose();
            }
        }
    }

    /**
     * Updates the interest
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doConnect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }
        updateInterestOps();
    }

    /**
     * <p>
     * Performs the read action on sc
     * </p>
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     * </p>
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doRead() throws IOException {
        activeSinceLastCheck = true;
        if (socketChannel.read(bufferIn) == -1) {
            logger.warning("client disconnected!");
            closed = true;
            silentlyClose();
            return;
        }
        processIn();
        updateInterestOps();

    }

    /**
     * <p>
     * Performs the write action on sc
     * </p>
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doWrite and after the call
     * </p>
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doWrite() throws IOException {
        activeSinceLastCheck = true;
        bufferOut.flip();
        socketChannel.write(bufferOut);
        bufferOut.compact();
        processOut();
        updateInterestOps();

    }

    /**
     * Close the client's channel
     */
    @Override
    public void silentlyClose() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    /**
     * Gets the client's socket
     *
     * @return the client's socket
     */
    @Override
    public SelectionKey getKey() {
        return this.key;
    }

    /**
     * Sets the connection's status
     */
    @Override
    public void close() {
        this.closed = true;
    }

    /**
     * Set
     *
     * @param visitor
     */
    protected void setVisitor(IPacketVisitor visitor) {
        this.visitor = visitor;
    }
}
