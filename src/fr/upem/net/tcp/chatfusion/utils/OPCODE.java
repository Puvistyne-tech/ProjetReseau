package fr.upem.net.tcp.chatfusion.utils;

public enum OPCODE {
    LOGIN_ANONYMOUS,
    LOGIN_PASSWORD,
    LOGIN_ACCEPTED,
    LOGIN_REFUSED,
    MESSAGE_PUBLIC,
    MESSAGE_PRIVATE,
    FILE_PRIVATE,
    FUSION_INIT,
    FUSION_INIT_OK,
    FUSION_INIT_KO,
    FUSION_INIT_FWD,
    FUSION_REQUEST,
    FUSION_REQUEST_RESP,
    FUSION_CHANGE_LEADER,
    FUSION_MERGE,
    //TODO
    ERROR,
    SHUTDOWN;

     public static OPCODE byteToOpcode(Byte b) {
        return OPCODE.values()[b];
    }
}

