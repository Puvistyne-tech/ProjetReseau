package fr.upem.net.tcp.chatfusion.server;

import fr.upem.net.tcp.chatfusion.context.IContext;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.Helpers;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerChatFusion {

    private static final Logger logger = Logger.getLogger(ServerChatFusion.class.getName());
    private static final long TIME_STAMP = 240_000;//4 min
    private final String name;
    private final FusionManger fusionManger;
    private SelectionKey myKey;
    private long ticks = 0;

    public SelectionKey getMyKey() {
        return myKey;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    private final ServerSocketChannel serverSocketChannel;

    private final InetSocketAddress inetSocketAddress;

    public List<String> getServerAsList() {
        return servers.keySet().stream().toList();
    }

    public HashMap<String, SelectionKey> getServers() {
        return servers;
    }

    private final HashMap<String, SelectionKey> servers = new HashMap<>();
    private final Map<String, SelectionKey> clients = new HashMap<>();

    private final Selector selector;

    private final Thread console;

    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(1);

    private final Object lock = new Object();

    public String getServerName() {
        return this.name;
    }

    public String getLeaderName() {
        return this.fusionManger.getLeader().name();
    }

    public ServerChatFusion(String name) throws IOException {
        this.name = Objects.requireNonNull(name);
        serverSocketChannel = ServerSocketChannel.open();
        var sc = new Scanner(System.in);
        serverSocketChannel.bind(null);
        this.inetSocketAddress = (InetSocketAddress) serverSocketChannel.getLocalAddress();

        System.out.println(serverSocketChannel.getLocalAddress());
        selector = Selector.open();

        this.console = new Thread(this::consoleRun);
        this.fusionManger = new FusionManger(this, inetSocketAddress);
        System.out.println("I am " + name + " on " + inetSocketAddress);

    }


    public FusionManger getFusionManger() {
        return fusionManger;
    }

    public void addServers(String name, SelectionKey key) {
        this.servers.put(name, key);
    }

    public void removeClient(String name) {
        this.clients.remove(name);
    }

    public void removeServer(String serverName) {
        this.servers.remove(serverName);

    }

    public void deleteServers() {
        this.servers.clear();
    }

    public void sendToLeader(Packet packet) {
        sendTo(this.fusionManger.getLeader().key(), packet);
    }

    public Map<String, SelectionKey> getClients() {
        return this.clients;
    }


    private enum Commands {
        INFO,
        SHUTDOWN,
        SHUTDOWNNOW,
        FUSION
    }

    private void consoleRun() {
        try {
            try (var scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    var command = scanner.nextLine();
                    sendCommands(command);
                }
            }
            logger.info("Console thread stopping");
        } catch (InterruptedException e) {
            logger.info("Console thread has been interrupted");
        }
    }

    private void sendCommands(String command) throws InterruptedException {
        synchronized (lock) {
            try {
                commandQueue.put(command);
                selector.wakeup();
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid Command");
            }
        }
    }

    private void receiveCommand() throws IOException {
        if (commandQueue.isEmpty()) return;

        var t = commandQueue.poll();
        var tmp = t.split(" ")[0].toUpperCase();
        try {
            switch (Commands.valueOf(tmp)) {
                case INFO -> showAllConnections();
                case SHUTDOWN -> serverSocketChannel.close();
                case SHUTDOWNNOW -> shutdownServer();
                case FUSION -> {
                    if (t.split(" ").length < 2) return;
                    var host = t.split(" ")[1];
                    var port = t.split(" ")[2];
                    this.getFusionManger().startFusion(host, Integer.parseInt(port));
                }
                default -> logger.warning("Invalid Command");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Bad command, Try again please");
        }
        selector.wakeup();

    }

    private void showAllConnections() {
        System.out.println("Clients Connected [" + clients.size() + "]");
        clients.keySet().forEach(System.out::println);
        System.out.println("Servers Connected [" + servers.size() + "]");
        System.out.println("Leader ::: " + this.getLeaderName());
        servers.keySet().forEach(System.out::println);
    }

    private void shutdownServer() throws IOException {
        logger.info("Good Bye...");
        selector.keys().forEach(this::silentlyClose);
        serverSocketChannel.close();
        Thread.currentThread().interrupt();
    }


    public void addClients(String login, SelectionKey key) {
        this.clients.put(login, key);
    }


    public void launch() throws IOException {
        serverSocketChannel.configureBlocking(false);
        this.myKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        console.start();

        while (!Thread.interrupted()) {
//            Helpers.printKeys(selector); // for debug
            System.out.println("Starting select");
            try {
                var startTime = System.currentTimeMillis();
                selector.select(this::treatKey, TIME_STAMP);
                ticks += System.currentTimeMillis() - startTime;
                if (ticks > TIME_STAMP) {
                    handleInactive();
                    ticks %= TIME_STAMP;
                }
                receiveCommand();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
            System.out.println("Select finished");
        }
    }

    public boolean ifClientAlreadyConnected(String client) {
        return clients.containsKey(client);
    }

    private void treatKey(SelectionKey key) {
//        Helpers.printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isConnectable()) {
                ((IContext) key.attachment()).doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                ((IContext) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((IContext) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doAccept(SelectionKey key) throws IOException {

        var sc = serverSocketChannel.accept();

        if (sc == null) {
            logger.warning("Selector Lied");
            return;
        }

        sc.configureBlocking(false);
        var clientKey = sc.register(selector, SelectionKey.OP_READ);

        var clientContext = new Context(this, clientKey);
        clientKey.attach(clientContext);
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = (Channel) key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }


    /**
     * Add a message to all connected clients queue
     *
     * @param packet
     */
    public void broadcastToClient(SelectionKey key, Packet packet) {
        clients.values().forEach(k -> {
            var cntx = (IContext) k.attachment();
            if (cntx != null && cntx.getKey() != key) {
                cntx.queueMessage(packet);
            }
        });

    }

    public void broadcastToServer(SelectionKey keySource, Packet packet) {
        servers.values().forEach(one -> {
            var cntx = (IContext) one.attachment();
            if (cntx != null && !one.equals(keySource)) {
                cntx.queueMessage(packet);
            }
        });
    }


    public void sendTo(String destination, Packet packet) {
        var context = (IContext) clients.get(destination).attachment();
        if (context != null)
            context.queueMessage(packet);
    }

    public void sendTo(SelectionKey key, Packet packet) {
        var context = (IContext) key.attachment();
        if (context != null)
            context.queueMessage(packet);
    }

    public void sendToServer(String server, Packet packet) {
        var sk = getServerKey(server);
        if (sk != null) {
            var cnt = (IContext) sk.attachment();
            if (cnt != null) {
                cnt.queueMessage(packet);
            }
        }

    }

    public void handleInactive() {
        selector.keys().stream()
                .filter(key -> key.channel() != serverSocketChannel)
                .forEach(key -> {
                    var ctx = (Context) key.attachment();
                    if (!ctx.isActiveSinceLastCheck()) {
                        try {
                            logger.info("Closing inactive connection from " + ctx.getSocketChannel().getRemoteAddress());
                            removeAllByKey(key);
                            key.channel().close();
                        } catch (IOException e) {
                            // Do nothing
                        }
                    } else {
                        ctx.setActiveSinceLastCheck(false);
                    }
                });
    }

    private void removeAllByKey(SelectionKey key) {
        servers.values().remove(key);
        this.clients.values().remove(key);
    }

    private SelectionKey getServerKey(String server) {
        return servers.get(server);
    }

    public SelectionKey registerAKey(InetSocketAddress address) {
        SelectionKey newKey = null;
        try {
            var sc = SocketChannel.open();
            sc.configureBlocking(false);
            newKey = sc.register(selector, SelectionKey.OP_WRITE);
            sc.connect(address);
            var newContext = new Context(this, newKey);
            newKey.attach(newContext);
//            logger.info("Key created ");
            if (sc.finishConnect()) {
                System.out.println("Connection Established");
            }
            return newKey;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendTo(InetSocketAddress address, Packet packet) {

        var key = registerAKey(address);
        var context = (Context) key.attachment();
        if (context != null) {
            context.queueMessage(packet);
            selector.wakeup();
        }
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1) {
            usage();
            return;
        }

        new ServerChatFusion(args[0]).launch();

    }

    private static void usage() {
        System.out.println("Usage : Server name");
    }

}

