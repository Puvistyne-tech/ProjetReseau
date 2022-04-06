package fr.upem.net.tcp.chatfusion.utils;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

public class Commander {
    static private final String PUBLIC = "", PRIVE = "DM", CONNEXION_PRIVE = "/@", OK_REQUEST = "ACC", KO_REQUEST = "REF";

    public static Packet commande(String string){

        return new PublicMessagePacket("server","login",string);
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