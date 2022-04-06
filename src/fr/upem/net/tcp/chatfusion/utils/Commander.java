package fr.upem.net.tcp.chatfusion.utils;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;

public class Commander {
    static private final String PUBLIC = "", PRIVE = "DM", CONNEXION_PRIVE = "/@", OK_REQUEST = "ACC", KO_REQUEST = "REF";

    public static Packet commande(String string){

        return new PublicMessagePacket("server","login",string);
    }
}
