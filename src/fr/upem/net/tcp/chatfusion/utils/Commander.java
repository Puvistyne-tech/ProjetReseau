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

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.client.ClientChatFusion;
import fr.upem.net.tcp.chatfusion.exception.UnknownInputReceivedException;
import fr.upem.net.tcp.chatfusion.packet.PrivateFilePacket;
import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * => @login:server message
 * => /login:server file
 * => message public
 */

public class Commander {

    public static OPCODE readFromTerminal(String input) {
        if (input.startsWith("@")) {
            return OPCODE.MESSAGE_PRIVATE;
        } else if (input.startsWith("/")) {
            return OPCODE.FILE_PRIVATE;
        } else return OPCODE.MESSAGE_PUBLIC;
    }

    public static PrivateMessagePacket treatPrivateMessage(String serverSource, String loginSource, String input) {
        var in = input.substring(1);
        var login = in.split(":")[0];
        var server = in.split(":")[1].split(" ")[0];
        var other = in.split(" ", 2)[1];

        if (input.charAt(0) == '@') {
            return new PrivateMessagePacket(serverSource, loginSource, server, login, other);
        } else throw new UnknownInputReceivedException();
    }

    public static PublicMessagePacket treatPublicMessage(String serverSource, String loginSource, String input) {
        if (input.charAt(0) != '@' && input.charAt(0) == '/') {
            return new PublicMessagePacket(serverSource, loginSource, input);
        } else throw new UnknownInputReceivedException();
    }

    public static List<PrivateFilePacket> treatPrivateFile(ClientChatFusion client, String input) {

        if (input.charAt(0) == '/') {
            var serverSource = client.getUniqueContext().getServerName();
            var loginSource = client.getLogin();
            var PATH = client.getPath();

            var in = input.substring(1);
            var login = in.split(":")[0];
            var server = in.split(":")[1].split(" ")[0];
            var other = in.split(" ", 2)[1];

            try (FileChannel fc = FileChannel.open(Path.of(other), StandardOpenOption.READ)) {

                var size = (int) fc.size();

                var header = new Buffer.Builder()
                        .addOpCode(OPCODE.FILE_PRIVATE)
                        .addString(serverSource)
                        .addString(loginSource)
                        .addString(server)
                        .addString(login)
                        .addString(other)
                        .addInt(0)
                        .addInt(0)
                        .build();

                var bufferSize = 1024 - header.remaining();

                var nbBuffers = size / bufferSize;
                if (size % bufferSize != 0) nbBuffers++;


                ByteBuffer[] bytes = new ByteBuffer[nbBuffers];

                for (int i = 0; i < nbBuffers; i++) {
                    bytes[i] = ByteBuffer.allocate(bufferSize);
                }

                while (fc.read(bytes) > 0) {
                    ;
                }

                List<PrivateFilePacket> fileList = new ArrayList<>();

                for (int i = 0; i < nbBuffers; i++) {
                    var t=bytes[i].flip().remaining();
                    var p = new PrivateFilePacket(
                            serverSource,
                            loginSource,
                            server,
                            login,
                            other,
                            nbBuffers,
                            t,
                            ByteBuffer.allocate(t).put(bytes[i])
                    );

                    fileList.add(p);
                }
                return fileList;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }


    private static String stringFromFile(Path path) {
        ByteBuffer byteBuffer;
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            byteBuffer = ByteBuffer.allocate((int) fc.size());
            while (byteBuffer.hasRemaining()) {
                fc.read(byteBuffer);
            }
            byteBuffer.flip();
            return UTF_8.decode(byteBuffer).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getLogin(String[] args) {
        var sc = new Scanner(System.in);
        System.out.println("Enter a password for login " + args[3] + " ::: ");

        String[] login;

        if (sc.hasNextLine()) {
            var pass = sc.nextLine();
            if (pass.length() > 3) {
                login = new String[]{args[0], args[1], args[2], args[3], pass};
                return login;
            }
        }
        login = new String[]{args[0], args[1], args[2], args[3]};
        return login;
    }
}
