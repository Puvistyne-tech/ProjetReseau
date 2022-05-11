package fr.upem.net.tcp.chatfusion.visitor;


import fr.upem.net.tcp.chatfusion.server.Context;
import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.*;
import fr.upem.net.tcp.chatfusion.server.FusionManger;
import fr.upem.net.tcp.chatfusion.server.ServerChatFusion;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 *
 */
public class ServerVisitor implements IPacketVisitor {

    private final ServerChatFusion serverChatFusion;
    private final Context context;
    private boolean authenticated = false;
    private static final Logger LOG = Logger.getLogger(ServerVisitor.class.getName());
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    private final FusionManger fusionManger;


    public ServerVisitor(ServerChatFusion server, Context context) {
        this.context = context;
        this.serverChatFusion = server;
        this.fusionManger = server.getFusionManger();
    }


//    private boolean inList() {
//        var key = context.getKey();
//        boolean result = false;
//
//        if (serverChatFusion.getServers().containsValue(key) || serverChatFusion.getClients().values().contains(key)) {
//            result = true;
//        }
//        return result;
//
//    }

    @Override
    public void visit(Packet packet) {

        if (authenticated) {

            switch (packet) {
                /////////////////////////////////////////////////////////////////////////////////////

                case PublicMessagePacket value -> {
                    if (fusionManger.AmILeader()) {
                        serverChatFusion.broadcastToClient(context.getKey(), value);
                        serverChatFusion.broadcastToServer(context.getKey(), value);
                    } else {
                        serverChatFusion.sendToLeader(value);
                    }
                }
                case PrivateMessagePacket prvtMsgP -> {
                    if (isItForMe(prvtMsgP.severDestination())) {
                        var loginDest = prvtMsgP.loginDestination();
                        serverChatFusion.sendTo(loginDest, prvtMsgP);
                    } else {
                        if (fusionManger.AmILeader()) {
                            this.serverChatFusion.sendToServer(prvtMsgP.severDestination(), prvtMsgP);
                        } else {
                            serverChatFusion.sendToLeader(prvtMsgP);
                        }
                    }
                }
                case PrivateFilePacket value -> {
                    LOG.info("(" + value.getServerSource() + " : " + value.getLoginSource() + ")" + dtf.format(LocalDateTime.now()) + " ::: " + value.getFilename());
                    if (isItForMe(value.getServerSource())) {
                        var loginDest = value.getLoginDestination();
                        serverChatFusion.sendTo(loginDest, value);
                    } else {
                        if (fusionManger.AmILeader()) {
                            this.serverChatFusion.sendToServer(value.getServerDestination(), value);
                        } else {
                            serverChatFusion.sendToLeader(value);
                        }
                    }
                }


                /////////////////////////////////////////////////////////////////////////////////////////////////////

                default -> throw new BadPacketReceivedException();
            }

        } else {
            switch (packet) {
                case LoginAnonymousPacket value -> {
                    var clientName = value.getLogin();
                    if (serverChatFusion.ifClientAlreadyConnected(clientName)) {
                        serverChatFusion.sendTo(context.getKey(), new LoginRefusedPacket());
//                        context.close();
                    } else {
                        context.setLogin(clientName);
                        serverChatFusion.addClients(clientName, context.getKey());
                        serverChatFusion.sendTo(context.getKey(), new LoginAcceptedPacket(serverChatFusion.getLeaderName()));
                        authenticated = true;
                    }
                }
                case LoginPasswordPacket clientP -> {
                    var clientName = clientP.getLogin();
                    if (serverChatFusion.ifClientAlreadyConnected(clientName)) {
                        serverChatFusion.sendTo(context.getKey(), new LoginRefusedPacket());
//                        context.close();
                    } else {
                        context.setLogin(clientName);
                        serverChatFusion.addClients(clientName, context.getKey());
                        serverChatFusion.sendTo(context.getKey(), new LoginAcceptedPacket(serverChatFusion.getLeaderName()));
                        authenticated = true;
                    }
                }

                /////////////////////////////////////////////////////////////////////////////////////
                case PublicMessagePacket value -> {
                    if (fusionManger.AmILeader()) {
                        serverChatFusion.broadcastToClient(context.getKey(), value);
                        serverChatFusion.broadcastToServer(context.getKey(), value);
                    } else {
                        serverChatFusion.sendToLeader(value);
                    }
                }
                case PrivateMessagePacket prvtMsgP -> {
                    if (isItForMe(prvtMsgP.severDestination())) {
                        var loginDest = prvtMsgP.loginDestination();
                        serverChatFusion.sendTo(loginDest, prvtMsgP);
                    } else {
                        if (fusionManger.AmILeader()) {
                            this.serverChatFusion.sendToServer(prvtMsgP.severDestination(), prvtMsgP);
                        } else {
                            serverChatFusion.sendToLeader(prvtMsgP);
                        }
                    }
                }
                case PrivateFilePacket value -> {
                    LOG.info("(" + value.getServerSource() + " : " + value.getLoginSource() + ")" + dtf.format(LocalDateTime.now()) + " ::: " + value.getFilename());
                    if (isItForMe(value.getServerSource())) {
                        var loginDest = value.getLoginDestination();
                        serverChatFusion.sendTo(loginDest, value);
                    }
                }
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                case FusionInitPacket value -> {
                    LOG.info("Received Fusion Request from server " + value.getServerSrc());
                    fusionManger.initFusion(value, context);
                }
                case FusionInitOKPacket value -> {
                    LOG.info("Fusion OK received. Starting Fusion ....");
                    fusionManger.initFusion(value, context);
                }
                case FusionInitKOPacket value -> {
                    LOG.info("Fusion request denied by the server");
                }
                case FusionInitFWDPacket value -> {
                    LOG.info("Retrying to the méga server of ::: " + value.leaderAddress());
                    this.fusionManger.forwardedFusionInit(value);
                }
                case FusionRequestPacket value -> {
                    LOG.info("Getting a Fusion Request from a member server");
                    fusionManger.initFusion(value, context);
                }
                case FusionRequestResPacket value -> {
                    if (value.status() == 0) {
                        LOG.info("Fusion is denied by the Méga server");
                    } else {
                        LOG.info("Fusion is accepted by the mega server");
                    }
                }
                case FusionChangeLeaderPacket value -> {
                    fusionManger.changeLeaderRequest(value);
                }
                case FusionMergePacket value -> {
                    System.out.println("Getting merge request from " + value.name());
                    fusionManger.merge(value, context);
                }
                default -> throw new BadPacketReceivedException();
            }

        }
    }

    private boolean isItForMe(String severDestination) {
        return this.serverChatFusion.getServerName().equals(severDestination);
    }

}
