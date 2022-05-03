package fr.upem.net.tcp.chatfusion.visitor;

import fr.upem.net.tcp.chatfusion.packet.*;

/**
 * Interface to do actions related to every Packet
 */
public interface IPacketVisitor {
//    void visitMessagePublicPacket(PublicMessagePacket publicMessagePacket);
//    void visitMessagePrivateMessagePacket(PrivateMessagePacket privateMessagePacket);
//    void visitAnonymousLoginPacket(LoginAnonymousPacket loginAnonymousPacket);
//    void visitLoginPasswordPacket(LoginPasswordPacket loginPasswordPacket);
    void visit(Packet packet);
}
