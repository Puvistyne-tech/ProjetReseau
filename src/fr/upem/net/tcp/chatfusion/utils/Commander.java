package fr.upem.net.tcp.chatfusion.utils;

import fr.upem.net.tcp.chatfusion.packet.LoginAnonymousPacket;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

import java.util.regex.Pattern;

public class Commander {
    static private final String PATTERN_MESSAGE = "([/|@]?)([A-Za-z0-9_]*):([A-Za-z0-9_]*)\s+(.*)";

    public static Packet commande(String input) {
//        var pattern = Pattern.compile(PATTERN_MESSAGE);
//        var matcher = pattern.matcher(input);
//
//        var loginDest = matcher.group(2);
//        var serverDest = matcher.group(3);
//        var textMessage = matcher.group(4);
//
//        return switch (matcher.group(1)) {
//            case "@" -> new PrivateMessagePacket(serverDest, loginDest, serverDest, loginDest, textMessage);
////            case "/"-> null;
//            default -> new PublicMessagePacket(serverDest, loginDest, textMessage);
//
//        };


        if (input.startsWith("@")) {
            var t = input.substring(1);
            if (t.contains(":")) {
                var tt = t.split(":");
                var login = tt[0];
                var ttt = tt[1].trim().split(" ", 2);
                var server = ttt[0];
                var text = ttt[1];

                return new PublicMessagePacket(server, login, text);
            }

        }
//        return new PublicMessagePacket("Dummy", "Login", input);
        return new LoginAnonymousPacket("Dummy");


//        return null;
    }

    public static String[] getLogin(String[] args){
        //TODO login from the terminal

        String[] login={"serverD","port","DummyLogin"};
        String[] loginPassword={"serverD","port","DummyLogin"};

        return login;
    }

}


/**
 * Les commandes tapées par l'utilisateur seront interprétées comme suit :
 * <p>
 * si la commande ne commence par / ou @, elle est interprétée comme un message public ;
 * si la commande est de la forme @login:server message, le reste de la commande est interprétée comme un message privé pour l'utilisateur login sur le serveur server
 * si la commande est la forme /login:server file, la commande est interprétée comme l'envoi du fichier file pour l'utilisateur login sur le serveur server
 * Votre serveur devra avoir une console. Pour demander la fusion d'un serveur avec le serveur à l'adresse ipaddress:port, il suffira de taper dans la console:
 */