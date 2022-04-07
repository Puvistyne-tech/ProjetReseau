package fr.upem.net.tcp.chatfusion.utils;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PrivateFilePacket;
import fr.upem.net.tcp.chatfusion.packet.PrivateMessagePacket;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

import java.util.regex.Pattern;

public class Commander {
    private static final String PATTERN_MESSAGE = "([/|@]?)([A-Za-z0-9_]*):([A-Za-z0-9_]*)\s+(.*)";

    static Packet commande(String loginSrc, String serverSrc, String input) {
        var pattern = Pattern.compile(PATTERN_MESSAGE);
        var matcher = pattern.matcher(input);

        var loginDest = matcher.group(2);
        var serverDest = matcher.group(3);

        switch (matcher.group(1)) {
            case "@" : {
                // Envoie d'un message texte privé
                var textMessage = matcher.group(4);
                return new PrivateMessagePacket(
                        serverSrc, loginSrc, serverDest, loginDest, textMessage);
            }

            case "/" : {
                // Envoie d'un fichier (privé)
                // Définir méthode pour partionnement en block du fichier si trop grand
                var filePath = matcher.group(4);
                /*
                return new PrivateFilePacket(
                        serverSrc, loginSrc, serverDest, loginDest, ... );
                */
                break;
            }

            default :
                // Un message publique
                var textMessage = matcher.group(4);
                return new PublicMessagePacket(serverSrc, loginSrc, textMessage);

        }
    }

    public static void main(String[] args) {

    }
}

//javba redex
// mge pub
/**
 * mgs pub : message
 * mgs pri : @login:server message
 * fichier pub : login:server fichier
 * ficher pri : /login:server file
 */

/**
 * Les commandes tapées par l'utilisateur seront interprétées comme suit :
 *
 * si la commande ne commence par / ou @, elle est interprétée comme un message public ;
 * si la commande est de la forme @login:server message, le reste de la commande est interprétée comme un message privé pour l'utilisateur login sur le serveur server
 * si la commande est la forme /login:server file, la commande est interprétée comme l'envoi du fichier file pour l'utilisateur login sur le serveur server
 * Votre serveur devra avoir une console. Pour demander la fusion d'un serveur avec le serveur à l'adresse ipaddress:port, il suffira de taper dans la console:
 */