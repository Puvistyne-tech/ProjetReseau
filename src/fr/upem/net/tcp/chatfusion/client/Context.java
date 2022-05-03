package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.reader.*;
import fr.upem.net.tcp.chatfusion.visitor.ClientVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;


public class Context implements IContext {
    static private final int BUFFER_SIZE = 10_000;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    private String serverName;

    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    public final ArrayDeque<Packet> queue = new ArrayDeque<>();
    private boolean closed = false;
    private final ClientVisitor visitor;

//    private ClientVisitor visitor;

    public Context(SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        visitor = new ClientVisitor(this);
    }

    /**
     * Process the content of bufferIn
     * <p>
     * The convention is that bufferIn is in write-mode before the call to process
     * and after the call
     */

    public void processIn() {

        var reader = new PacketReader();

        for (; ; ) {
//
//            Reader<? extends Packet> reader = null;
//            switch (opCodeReader) {
//                case MESSAGE_PUBLIC:
//                    publicMessagehandler(new PublicMessageReader());
//                    visitor.visit(reader);
//                    break;
//                case MESSAGE_PRIVATE:
//                    privateMessagehandler( new PrivateMessageReader());
//                    break;
//                case LOGIN_ANONYMOUS:
//                    handleLoginAnonymous(new LoginAnonymousReader());
//                    break;
//                case LOGIN_PASSWORD:
//                    handleLoginPassword(new LoginPasswordReader());
//                    break;
//                case LOGIN_ACCEPTED: {
//                    reader = new StringReader();
//                    if (reader.process(bufferIn) == Reader.ProcessStatus.DONE) {
//                        this.server = ((StringPacket) reader.get()).message();
//                        System.out.println("Authenticated with the server :: " + server);
//                        reader.reset();
//                        return;
//                    }
//                    break;
//                }
//                case LOGIN_REFUSED: {
//                    System.out.println("Login Refused, Please Try again");
//                    silentlyClose();
//                    return;
//                }
//                default:
//                    System.out.println("Something is wrong shutting down the client");
//                    closed=true;
//                    silentlyClose();
//                    return;
//            }

            assert reader != null;
            Reader.ProcessStatus status = reader.process(bufferIn);
            switch (status) {
                case DONE:
                    var value = reader.get();
                    reader.reset();
                    value.accept(visitor);
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
     * Add a message to the message queue, tries to fill bufferOut and updateInterestOps
     *
     * @param packet
     */
    public void queueMessage(Packet packet) {

        if (!queue.isEmpty()) {
            return;
        }
        queue.offer(packet);
        processOut();
        updateInterestOps();
    }

    /**
     * Try to fill bufferOut from the message queue
     */
    public void processOut() {
        //TODO
        while (!queue.isEmpty() && bufferOut.hasRemaining()) {
            var message = queue.peek();

            var mgsBuffer = message.toByteBuffer();

            if (mgsBuffer.remaining() <= bufferOut.remaining()) {
                bufferOut.put(mgsBuffer);
                queue.pop();
            } else {
                return;
            }
        }

    }

    /**
     * Update the interestOps of the key looking only at values of the boolean
     * closed and of both ByteBuffers.
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * updateInterestOps and after the call. Also it is assumed that process has
     * been be called just before updateInterestOps.
     */

    public void updateInterestOps() {
        // TODO
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
        }
        key.interestOps(interestOps);
    }

    public void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }


    //Getters and Setters

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public SelectionKey getKey() {
        return this.key;
    }

    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     *
     * @throws IOException
     */
    public void doRead() throws IOException {

        var t = sc.read(bufferIn);

        if (t == 0) {
            return;
        }
        if (t == -1) {
            System.out.println("Connection closed");
            closed = true;
        }
        System.out.println("==>client getting something" + bufferIn);

        processIn();
        updateInterestOps();

    }

    /**
     * Performs the write action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doWrite and after the call
     *
     * @throws IOException
     */

    public void doWrite() throws IOException {

        bufferOut.flip();

        System.out.println("<== sending" + bufferOut);
        sc.write(bufferOut);

        if (bufferOut.hasRemaining()) {
            System.out.println("writing error");
            return;
        }

        bufferOut.compact();

        processOut();
        updateInterestOps();
    }

    public void doConnect() throws IOException {

        if (!sc.finishConnect()) {
            return;
        }
        //
//        key.interestOps(SelectionKey.OP_READ);
        updateInterestOps();
    }


}