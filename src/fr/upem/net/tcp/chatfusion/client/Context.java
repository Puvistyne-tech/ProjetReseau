package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.context.AContext;
import fr.upem.net.tcp.chatfusion.visitor.ClientVisitor;

import java.nio.channels.SelectionKey;


/**
 * Context for Client
 */
public class Context extends AContext {

    private String serverName;

    private final ClientChatFusion clientChatFusion;

    public Context(ClientChatFusion clientChatFusion, SelectionKey key) {
        super(key);
        this.clientChatFusion = clientChatFusion;
        var visitor = new ClientVisitor( clientChatFusion,this);
        super.setVisitor(visitor);
    }



    /**
     *
     * Getters and Setters
     */

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    public String getServerName() {
        return serverName;
    }

}