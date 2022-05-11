package fr.upem.net.tcp.chatfusion.server;


import fr.upem.net.tcp.chatfusion.context.AContext;
import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.*;
import fr.upem.net.tcp.chatfusion.reader.*;
import fr.upem.net.tcp.chatfusion.visitor.ServerVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.logging.Logger;

/**
 * Context for Server
 */
public class Context extends AContext {

    private String login;

    private final ServerChatFusion serverChatFusion;

    /**
     * Gets the server chat fusion
     * @return the ChatFusion's server
     */
    public ServerChatFusion getServer() {
        return this.serverChatFusion;
    }

    /**
     * Constructs a context for a server
     * @param serverChatFusion  the ChatFusion's server
     * @param key               the server socket
     */
    public Context(ServerChatFusion serverChatFusion, SelectionKey key) {
        super(key);

        this.serverChatFusion = serverChatFusion;
        var visitor = new ServerVisitor(serverChatFusion,this);
        super.setVisitor(visitor);
    }

    /**
     * Sets the login
     * @param login
     */
    public void setLogin(String login){
        this.login = login;
    }

}
