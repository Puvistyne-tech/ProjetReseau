//package fr.upem.net.tcp.chatfusion.reader;
//
//import fr.upem.net.tcp.chatfusion.packet.StringPackets;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//
//import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;
//
//public class MessageReader implements Reader<StringPackets> {
//
//    private final static int BUFFER_SIZE = 1024;
//    private final static Charset CHARSET = StandardCharsets.UTF_8;
//
//    private enum State {
//        DONE, WAITING_USERNAME, WAITING_TEXT, ERROR
//    }
//
//    private State state = State.WAITING_USERNAME;
//    private final StringReader stringReader = new StringReader();
//    private StringPackets message;
//
//    @Override
//    public ProcessStatus process(ByteBuffer buffer) {
//        if (state == State.DONE || state == State.ERROR) {
//            throw new IllegalStateException();
//        }
//
//        String username = null;
//        String text = null;
//
//        if (state == State.WAITING_USERNAME) {
//            username = readString(buffer, State.WAITING_TEXT);
//        }
//        if (state == State.WAITING_TEXT) {
//            text = readString(buffer, State.DONE);
//        } else {
//            return REFILL;
//        }
//        if (text == null || username == null) {
//            return ERROR;
//        }
//
//        this.message = new StringPackets(username, text);
//        state = State.DONE;
//
//        return DONE;
//    }
//
//    private String readString(ByteBuffer buffer, State nextState) {
//        var status = stringReader.process(buffer);
//        if (status == DONE) {
//            var value = stringReader.get();
//            //stringReader.reset();
//            message=null;
//            stringReader.reset();
//            state = nextState;
//            return value;
//        }
//
////        if (status == REFILL){
////            return readString(buffer, nextState);
////        }
//        else return null;
//    }
//
//    public StringPackets get() {
//        if (state != State.DONE) {
//            throw new IllegalStateException();
//        }
//        return message;
//    }
//
//    public void reset() {
//        state = State.WAITING_USERNAME;
//        stringReader.reset();
//        message = null;
//    }
//}
