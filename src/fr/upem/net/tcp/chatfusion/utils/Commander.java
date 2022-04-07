/*
//package fr.upem.net.tcp.chatfusion.utils;
//
//import fr.upem.net.tcp.chatfusion.packet.LoginAnonymousPacket;
//import fr.upem.net.tcp.chatfusion.packet.Packet;
//import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;
//import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;
//
//import java.util.regex.Pattern;
//
//public class Commander {
//    static private final String PATTERN_MESSAGE = "([/|@]?)([A-Za-z0-9_]*):([A-Za-z0-9_]*)\s+(.*)";
//    private static final String PATTERN_PUBLIC_MESSAGE = "^(/@).*";
//
//
//    //    public static Packet commande(String input) {
//////        var pattern = Pattern.compile(PATTERN_MESSAGE);
//////        var matcher = pattern.matcher(input);
//////
//////        var loginDest = matcher.group(2);
//////        var serverDest = matcher.group(3);
//////        var textMessage = matcher.group(4);
//////
//////        return switch (matcher.group(1)) {
//////            case "@" -> new PrivateMessagePacket(serverDest, loginDest, serverDest, loginDest, textMessage);
////////            case "/"-> null;
//////            default -> new PublicMessagePacket(serverDest, loginDest, textMessage);
//////
//////        };
////
////
////        if (input.startsWith("@")) {
////            var t = input.substring(1);
////            if (t.contains(":")) {
////                var tt = t.split(":");
////                var login = tt[0];
////                var ttt = tt[1].trim().split(" ", 2);
////                var server = ttt[0];
////                var text = ttt[1];
////
////                return new PublicMessagePacket(server, login, text);
////            }
////
////        }
//////        return new PublicMessagePacket("Dummy", "Login", input);
////        return new LoginAnonymousPacket("Dummy");
////
////
//////        return null;
////    }
//    public static Packet commande(String input) {
//        var patternMessage = Pattern.compile(PATTERN_MESSAGE);
//        var patternPublicMessage = Pattern.compile(PATTERN_PUBLIC_MESSAGE);
//
//        var matcherMessage = patternMessage.matcher(input);
//        var matcherPublicMessage = patternPublicMessage.matcher(input);
//
//        if (matcherMessage.matches()) {
//            // Message privé
//            var loginDest = matcherMessage.group(3);
//            var serverDest = matcherMessage.group(4);
//
//            switch (matcherMessage.group(1)) {
//                case "@": {
//                    // Envoie d'un message texte privé
//                    var textMessage = matcherMessage.group(4);
//                    return new PrivateMessagePacket(
//                            serverDest, loginDest, serverDest, loginDest, textMessage);
//                }
////                default:
////                    // Envoie d'un fichier (privé)
////                    var filePath = matcherMessage.group(4);
////                    // 3 DERNIERS ARGUMENTS A CHANGER
////                    return new PrivateFilePacket(
////                            serverSrc,
////                            loginSrc,
////                            serverDest,
////                            loginDest,
////                            filePath,
////                            1,
////                            1,
////                            Byte.valueOf("0001"));
//            }
//        }
//        // Message publique
//        var textMessage = matcherPublicMessage.toString();
//        return new PublicMessagePacket(matcherMessage.group(4), matcherMessage.group(3), textMessage);
//    }
//

//
//}
*/



package fr.upem.net.tcp.chatfusion.utils;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PrivateFilePacket;
import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public class Commander {
    private static final String PATTERN_MESSAGE = "([/|@]?)([A-Za-z0-9_]+):([A-Za-z0-9_]+)\s+(.*)";
    private static final String PATTERN_PUBLIC_MESSAGE = "^(/@).*";

    public static Packet commande(String loginSrc, String serverSrc, String input) {
        var patternMessage = Pattern.compile(PATTERN_MESSAGE);
        var patternPublicMessage = Pattern.compile(PATTERN_PUBLIC_MESSAGE);

        var matcherMessage = patternMessage.matcher(input);
        var matcherPublicMessage = patternPublicMessage.matcher(input);

        if (matcherMessage.matches()) {
            // Message privé
            var loginDest = matcherMessage.group(3);
            var serverDest = matcherMessage.group(4);

            switch (matcherMessage.group(1)) {
                case "@": {
                    // Envoie d'un message texte privé
                    var textMessage = matcherMessage.group(4);
                    return new PrivateMessagePacket(
                            serverSrc, loginSrc, serverDest, loginDest, textMessage);
                }
                default:
                    // Envoie d'un fichier (privé)
                    var filePath = matcherMessage.group(4);
                    // 3 DERNIERS ARGUMENTS A CHANGER
                    return new PrivateFilePacket(
                            serverSrc,
                            loginSrc,
                            serverDest,
                            loginDest,
                            filePath,
                            1,
                            1,
                            Byte.valueOf("0001"));
            }
        }
        // Message publique
        var textMessage = matcherPublicMessage.toString();
        return new PublicMessagePacket(serverSrc, loginSrc, textMessage);
    }
        public static String[] getLogin(String[] args){
        //TODO login from the terminal

        String[] login={"localhost","7777","DummyLogin"};
        String[] loginPassword={"localhost","7777","pass","DummyLogin"};

        return loginPassword;
    }
}
