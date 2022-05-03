package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.*;
import static fr.upem.net.tcp.chatfusion.reader.Reader.ProcessStatus.ERROR;

public class PacketReader implements Reader<Packet> {
    private Reader<? extends Packet> reader;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        OpCodeReader opCodeReader = new OpCodeReader();
        var opCode = opCodeReader.process(buffer);
        if (opCode == DONE) {
            var op = opCodeReader.get();
            // var op = OpCodeHandler.getOpCode(opCode);
            switch (op) {
                case LOGIN_ANONYMOUS -> reader = new LoginAnonymousReader();
                case LOGIN_PASSWORD -> reader = new LoginPasswordReader();
                case LOGIN_ACCEPTED -> reader = new LoginAcceptedReader();
                case LOGIN_REFUSED -> reader = new LoginRefusedReader();
                case MESSAGE_PUBLIC -> reader = new PublicMessageReader();
                case MESSAGE_PRIVATE -> reader = new PrivateMessageReader();
                case FILE_PRIVATE -> reader = new PrivateFileReader();
                case FUSION_INIT -> reader = new FusionInitReader();
//            case FUSION_INIT_OK -> reader=new FusionInitOKReader();
//            case FUSION_INIT_KO -> reader=new FusionInitKOReader();
//            case FUSION_INIT_FWD -> reader=new FusionInitFWDReader();
//            case FUSION_REQUEST -> reader=new FusionRequestReader();
//            case FUSION_REQUEST_RESP -> reader=new FusionRequestResReader();
//            case FUSION_CHANGE_LEADER -> reader=new FusionChangeLeaderReader();
//            case FUSION_MERGE -> reader=new FusionMergeReader();
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

    @Override
    public Packet get() {
        if (reader == null) return null;
        return reader.get();
    }

    @Override
    public void reset() {
        //TODO
        // if(reader!=null)
        reader.reset();
    }

//    private OPCODE getOpCodeState(ByteBuffer buffer) {
//        OpCodeReader opCodeReader = new OpCodeReader();
//
//        var opCodeState = opCodeReader.process(buffer);
//        System.out.println(opCodeState);
//        if (opCodeState==ProcessStatus.DONE) {
//            var opCode = opCodeReader.get();
//            System.out.println(opCode);
//            return switch (opCode.value()) {
//                case 0 -> LOGIN_ANONYMOUS;
//                case 1 -> LOGIN_PASSWORD;
//                case 2 -> LOGIN_ACCEPTED;
//                case 3 -> LOGIN_REFUSED;
//                case 4 -> MESSAGE_PUBLIC;
//                case 5 -> MESSAGE_PRIVATE;
//                case 6 -> FILE_PRIVATE;
//                case 8 -> FUSION_INIT;
//                case 9 -> FUSION_INIT_OK;
//                case 10 -> FUSION_INIT_KO;
//                case 11 -> FUSION_INIT_FWD;
//                case 12 -> FUSION_REQUEST;
//                case 13 -> FUSION_REQUEST_RESP;
//                case 14 -> FUSION_CHANGE_LEADER;
//                case 15 -> FUSION_MERGE;
//                default -> throw new IllegalStateException("OP_code is not valid");
//            };
//        }else if(opCodeState== ERROR){
//            return OPCODE.ERROR;
//        }
//        return opCodeState;
//    }
}
