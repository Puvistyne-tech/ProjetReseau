package fr.upem.net.tcp.chatfusion.reader;

import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;

import static fr.upem.net.tcp.chatfusion.utils.OPCODE.*;

public class OpCodeHandler {


//    static public OPCODE getOpCode(ByteBuffer buffer) {
//        OpCodeReader opCodeReader = new OpCodeReader();
//
//        var opCodeState = opCodeReader.process(buffer);
//        System.out.println(opCodeState);
//        if (opCodeState.toString().equals("DONE")) {
//            var opCode = opCodeReader.get();
//            System.out.println(opCode);
//            return switch (opCode) {
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
//        }else if(opCodeState.toString().equals("REFILL")){
//            return SHUTDOWN;
//        }
//        else {
//            throw new IllegalStateException("OP_code is not valid");
//        }
//    }


}