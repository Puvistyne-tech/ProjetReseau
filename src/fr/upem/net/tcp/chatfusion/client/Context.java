package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.LoginPasswordPacket;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.reader.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;


public class Context implements IContext {
    static private final int BUFFER_SIZE = 10_000;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    public final ArrayDeque<Packet> queue = new ArrayDeque<>();
    private boolean closed = false;

    public Context(SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
    }

    /**
     * Process the content of bufferIn
     * <p>
     * The convention is that bufferIn is in write-mode before the call to process
     * and after the call
     */

    public void processIn() {


        var opCodeReader = OpCodeHandler.getOpCode(bufferIn);
        //if (opCodeReader.ordinal() >= 0 && opCodeReader.ordinal() < 20)
            for (; ; ) {

                Reader<? extends Packet> reader = null;

                switch (opCodeReader) {
                    case MESSAGE_PUBLIC -> reader = new PublicMessageReader();
                    case MESSAGE_PRIVATE -> reader = new PrivateMessageReader();
                    case LOGIN_ANONYMOUS -> reader = new LoginAnonymousReader();
                    case LOGIN_PASSWORD -> reader = new LoginPasswordReader();
                    case LOGIN_ACCEPTED -> reader = new StringReader();
                    case LOGIN_REFUSED -> {
                        silentlyClose();
                        return;
                    }
                }

                assert reader != null;
                Reader.ProcessStatus status = reader.process(bufferIn);
                switch (status) {
                    case DONE:
                        var value = reader.get();
                        var dtf = DateTimeFormatter.ofPattern("HH:mm");
                        System.out.println(dtf.format(LocalDateTime.now()) + " ::: " + value.toString());
                        reader.reset();
                        break;
                    case REFILL:
                        return;
                    case ERROR:
                        silentlyClose();
                        return;
                }
            }


//        for (; ; ) {
            /*

            Reader<Packet> reader = new PublicMessageReader();

            Reader.ProcessStatus status = reader.process(bufferIn);
            */
//            var reader = O
//            switch (status) {
//
//                case DONE:
//                    var message = reader.get();
//                    reader.reset();
//
//                    var dtf = DateTimeFormatter.ofPattern("HH:mm");
//                    System.out.println(dtf.format(LocalDateTime.now()) + " from " + message.username() + " ::: " + message.text());
//                    //System.out.println(message);
//
//                    break;
//                case REFILL:
//                    return;
//                case ERROR:
//                    silentlyClose();
//                    return;
//            }
//        }

//        while (bufferIn.hasRemaining()) {
//            switch (reader.process(bufferIn)) {
//                case ERROR:
//                    silentlyClose();
//                case REFILL:
//                    return;
//                case DONE:
//                    log(reader.get());
//                    reader.reset();
//                    break;
//            }
//        }
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

//            var userBuffer = CHARSET.encode(message.username());
            //          var textBuffer = CHARSET.encode(message.text());
//            var tmp = message.text();
//            System.out.println(message.text());
//
//
//            var textBuffer = ByteBuffer.allocate(tmp.length());
//            textBuffer.put(CHARSET.encode(tmp)).flip();
//
//            String nameServer = "DummyServer";
//            var serverBuff = ByteBuffer.allocate(nameServer.length());
//            serverBuff.put(CHARSET.encode(nameServer)).flip();
//
//            ByteBuffer mgsBuffer = ByteBuffer.allocate(serverBuff.capacity() + userBuffer.capacity() + textBuffer.capacity() + Integer.BYTES * 4);

            var mgsBuffer = message.toByteBuffer();
//            if (mgsBuffer.capacity() >= 1024) {
//                return;
//            }

//            mgsBuffer
//                    .putInt(4)
//                    .putInt(serverBuff.capacity())
//                    .put(serverBuff)
//                    .putInt(userBuffer.capacity())
//                    .put(userBuffer)
//                    .putInt(textBuffer.capacity())
//                    .put(textBuffer)
//                    .flip();

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

    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     *
     * @throws IOException
     */
    public void doRead() throws IOException {
        // TODO
        var t = sc.read(bufferIn);

        if (t == 0) {
            return;
        }
        if (t == -1) {
            System.out.println("Connection closed");
            closed = true;
        }

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

        System.out.println(bufferOut);
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
        key.interestOps(SelectionKey.OP_READ);
        //updateInterestOps();
    }


}