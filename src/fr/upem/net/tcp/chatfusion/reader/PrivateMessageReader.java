package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * MESSAGE_PRIVATE(5) =
 * 5 (OPCODE) server_src (STRING<=100)
 * login_src (STRING<=30)
 * server_dst (STRING<=100)
 * login_dest (STRING<=30)
 * msg (STRING<=1024)
 */

public class PrivateMessageReader implements Reader<PrivateMessagePacket> {

    private enum State {
        DONE,
        WAITING_SERVER_SRC,
        WAITING_USERNAME_SRC,
        WAITING_SERVER_DEST,
        WAITING_USERNAME_DEST,
        WAITING_TEXT,
        ERROR
    }

    private State state = State.WAITING_USERNAME_SRC;
    private final StringReader stringReader = new StringReader();
    private PrivateMessagePacket message;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {

        if (state==State.DONE||state==State.ERROR){
            throw new IllegalStateException();
        }

        buffer.flip();
//
//
//
//        try{
//            if ()
//        }

        return null;
    }

    @Override
    public PrivateMessagePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return message;
    }

    @Override
    public void reset() {
        state = State.WAITING_USERNAME_SRC;
        stringReader.reset();
        message = null;
    }
}
