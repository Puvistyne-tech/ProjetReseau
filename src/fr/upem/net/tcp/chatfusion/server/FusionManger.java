package fr.upem.net.tcp.chatfusion.server;

import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.exception.BadPacketReceivedException;
import fr.upem.net.tcp.chatfusion.packet.*;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Proceeds and manage the fusion
 */
public class FusionManger {

    private Server leader;
    private final ServerChatFusion currentServer;

    // future servers to be added to the cloud
    private final List<String> waitingList = new ArrayList<>();
    private boolean fusionOnGoing = false;
    private long fusionInitiated = 0;


    /**
     * 
     * @param currentServer
     * @param address
     */
    public FusionManger(ServerChatFusion currentServer, InetSocketAddress address) {
        this.currentServer = currentServer;
        this.leader = new Server(currentServer.getServerName(), address, currentServer.getMyKey());

    }

    private void checkTimeout() {
        if (fusionOnGoing && System.currentTimeMillis() - fusionInitiated >= 3000) {
            System.out.println("Fusion timeout reached, Cancelling all ongoing Fusions");
            terminateFusion();
        }
    }

    private void initiateFusion() {
        fusionOnGoing = true;
        this.fusionInitiated = System.currentTimeMillis();
    }

    private void terminateFusion() {
        fusionInitiated = 0;
        fusionOnGoing = false;
        waitingList.clear();
    }

    private void checkFusion() {
        var isOK = new HashSet<>(currentServer.getServerAsList()).containsAll(waitingList);
        if (isAnyFusionInProcess() && isOK) {
            System.out.println("Merging Successful");
            terminateFusion();
        } else if (!isAnyFusionInProcess() && isOK) {
            System.out.println("Merging Successful");
        } else if (isAnyFusionInProcess() && !isOK) {
            checkTimeout();
        } else {
            waitingList.forEach(currentServer::removeServer);
            waitingList.clear();
        }
    }

    public boolean AmILeader() {
        return this.leader.name().equals(currentServer.getServerName());
    }

    public Server getLeader() {
        return leader;
    }

    private void assignLeader(String server, InetSocketAddress address) {
        int comparator = server.compareTo(this.leader.name());
        if (comparator > 0) {
            addToWaitingList(server);
        } else if (comparator < 0) {
            currentServer.getServers().forEach((name, key) -> {
                currentServer.sendTo(key, new FusionChangeLeaderPacket(server, address));
                var cntx = (Context) key.attachment();
                cntx.close();
            });
            currentServer.deleteServers();
            System.out.println("<== FUS MERGE");
            this.leader = new Server(server, address, currentServer.registerAKey(address));
            currentServer.sendTo(this.leader.key(), new FusionMergePacket(currentServer.getServerName()));
            terminateFusion();
        } else {
            throw new IllegalStateException();
        }
    }

    private boolean checkMembers(List<String> members) {
        AtomicBoolean result = new AtomicBoolean(true);
        this.currentServer.getServerAsList().forEach(one -> {
            if (members.contains(one)) {
                result.set(false);
            }
        });
        return result.get();
    }

    public void addToWaitingList(List<String> members) {
        this.waitingList.addAll(members);
    }

    public void addToWaitingList(String member) {
        this.waitingList.add(member);
    }

    public boolean isAnyFusionInProcess() {
        return this.fusionOnGoing;
    }

    public void initFusion(FusionInitPacket fusionInitPacket, IContext context) {
        while (true) {
            checkTimeout();
            if (!isAnyFusionInProcess()) {
                initiateFusion();
                if (AmILeader()) {
                    if (checkMembers(fusionInitPacket.getMembers()) && checkMembers(List.of(fusionInitPacket.getServerSrc()))) {
                        addToWaitingList(fusionInitPacket.getMembers());

                        context.queueMessage(
                                new FusionInitOKPacket(
                                        this.currentServer.getServerName(),
                                        this.currentServer.getInetSocketAddress(),
                                        this.currentServer.getServerAsList()
                                ));
                        assignLeader(fusionInitPacket.getServerSrc(), fusionInitPacket.getSocketAddress());
                    } else {
                        context.queueMessage(new FusionInitKOPacket());
                        terminateFusion();
                    }
                } else {
                    System.out.println("<== INIT FWD");
                    context.queueMessage(new FusionInitFWDPacket(this.leader.address()));
                    terminateFusion();
                }
                break;
            }
        }
    }

    public void initFusion(FusionInitOKPacket fusionInitOKPacket, IContext context) {
        checkTimeout();
        if (isAnyFusionInProcess()) {
            if (AmILeader()) {
                if (checkMembers(fusionInitOKPacket.getMembers()) && checkMembers(List.of(fusionInitOKPacket.getServerSrc()))) {
                    addToWaitingList(fusionInitOKPacket.getMembers());
                    assignLeader(fusionInitOKPacket.getServerSrc(), fusionInitOKPacket.getSocketAddress());
                } else {
                    System.out.println("Fusion not successful");
                }
            } else {
                System.out.println("Wrong .... Asking the leader for the fusion, Ignoring...");
            }
        }
    }

    public void initFusion(FusionRequestPacket fusionRequestPacket, IContext context) {
        if (!AmILeader()) {
            throw new BadPacketReceivedException();
        }
        checkTimeout();
        if (!isAnyFusionInProcess()) {
            initiateFusion();

            currentServer.sendTo(fusionRequestPacket.address(),
                    new FusionInitPacket(
                            this.leader.name(),
                            this.leader.address(),
                            this.currentServer.getServerAsList()
                    )
            );

        } else {
            System.out.println("MEGA :: Fusion is not possible now");
            context.queueMessage(new FusionRequestResPacket((byte) 0));
        }
    }

    public void changeLeaderRequest(FusionChangeLeaderPacket value) {
        var address = value.address();
        var name = value.leader();
        this.leader = new Server(name, address, currentServer.registerAKey(address));
        currentServer.sendTo(address, new FusionMergePacket(currentServer.getServerName()));
    }

    public void merge(FusionMergePacket fusionMergePacket, Context context) {
        checkTimeout();
        if (isAnyFusionInProcess() && AmILeader()) {
            var serverName = fusionMergePacket.name();
            if (this.waitingList.contains(serverName)) {
                this.currentServer.addServers(serverName, context.getKey());
                checkFusion();
                System.out.println("Merging Successfull ::: " + serverName);
            } else
                System.out.println("Merging unsuccessfull -- " + serverName);
        } else {
            System.err.println("REAlly BaD");
            throw new BadPacketReceivedException();
        }
    }

    public void forwardedFusionInit(FusionInitFWDPacket value) {
        if (AmILeader()) {
            checkTimeout();
            if (isAnyFusionInProcess()) {
                System.out.println("==> FWD :: Sending to leader");
                currentServer.sendTo(value.leaderAddress(),
                        new FusionInitPacket(
                                this.leader.name(),
                                this.leader.address(),
                                this.currentServer.getServerAsList()
                        )
                );
            } else {
                System.out.println("Wrong place to FWD");
            }
        }
    }

    public void startFusion(String host, int port) {
        var address=new InetSocketAddress(host, port);
        if(address.equals(currentServer.getInetSocketAddress())){
            System.err.println("Can not fusion with yourself ... OOPS");
            return;
        }
        if (AmILeader()) {
            if (!isAnyFusionInProcess()) {
                initiateFusion();
                System.out.println("####Starting Fusion INIT");
                var p = new FusionInitPacket(this.currentServer.getServerName(), this.currentServer.getInetSocketAddress(), currentServer.getServerAsList());

                currentServer.sendTo(address, p);
            }
        } else {
            System.out.println("#### FUS REQ ");
            currentServer.sendToLeader(
                    new FusionRequestPacket(
                            new InetSocketAddress(host, port)
                    )
            );
        }
    }
}
