package fr.upem.net.tcp.chatfusion.server;

import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.Helpers;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private String leader;
    private final String name;
    private final ServerSocketChannel serverSocketChannel;

    private final HashMap<String, SocketAddress> servers = new HashMap<>();

    private final Selector selector;

    private final Thread console;
    private final Thread fusion;
    private final ArrayBlockingQueue<Commands> commandQueue = new ArrayBlockingQueue<>(1);

    private final Map<String, SocketAddress> clients = new HashMap<String, SocketAddress>();

    private final Object lock = new Object();

    public String getServerName(){
        return this.name;
    }
    public String getLeaderName(){
        return this.leader;
    }

    public Server(String name) throws IOException {
        this.name = Objects.requireNonNull(name);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(7777));

        System.out.println(serverSocketChannel.getLocalAddress());
        selector = Selector.open();
        servers.put(name, serverSocketChannel.getLocalAddress());

        this.leader = name;
        this.console = new Thread(this::consoleRun);
        this.fusion = new Thread(this::fusionRun);
    }

    private void fusionRun() {

    }

    private enum Commands {
        INFO,
        SHUTDOWN,
        SHUTDOWNNOW,
        MERGE
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
        try {
            synchronized (lock) {
                commandQueue.put(Commands.valueOf(command.toUpperCase()));
                selector.wakeup();
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid Command");
        }
    }

    private void receiveCommand() throws IOException {
        if (commandQueue.isEmpty()) return;

        switch (commandQueue.poll()) {
            case INFO -> showAllClients();
            case SHUTDOWN -> serverSocketChannel.close();
            case SHUTDOWNNOW -> shutdownServer();
            case MERGE -> fusion.start();
            default -> logger.warning("Invalid Command");
        }
    }

    private void showAllClients() {
        System.out.println("Clients Connected");
        AtomicInteger count = new AtomicInteger();
        selector.keys().forEach(oneKey -> {
            var context = (Context) oneKey.attachment();
            var client = oneKey.channel();
            if (client != serverSocketChannel && !context.isClosed()) {
                System.out.println(context);
                count.getAndIncrement();
            }
        });
        System.out.println("\nnumber of clients connected :: " + count);
    }

    private void shutdownServer() throws IOException {
        logger.info("Good Bye...");
        selector.keys().forEach(this::silentlyClose);
        serverSocketChannel.close();
        Thread.currentThread().interrupt();
    }

    private String defineLeader() {
        return servers.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted().findFirst().orElseThrow(RuntimeException::new);
    }

    public void addClients(String login, SocketAddress address) {
        this.clients.put(login,address);
    }

    public void launch() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        console.start();

        while (!Thread.interrupted()) {
            Helpers.printKeys(selector); // for debug
            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
                receiveCommand();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
            System.out.println("Select finished");
        }
    }

    public boolean ifClientAlreadyConnected(String client){
        System.out.println(clients);
        var t=clients.containsKey(client);
        System.out.println(t);
        return t;
    }

    private void treatKey(SelectionKey key) {
        Helpers.printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {

                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
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

        var clientContext=new Context(this,clientKey);
        clientKey.attach(clientContext);
        //addClients(,clientContext);
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
    public void broadcast(Packet packet) {
        // TODO

        selector.keys().forEach(k -> {
            if (k.channel() == serverSocketChannel) return;

            var context = (Context) k.attachment();

            if (context != null)
                (context).queueMessage(packet);


        });
    }

    public void sendTo(SelectionKey key,Packet packet){
        var context = (Context) key.attachment();

        if (context != null)
            (context).queueMessage(packet);
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1) {
            usage();
            return;
        }

        new Server(args[0]).launch();
    }

    private static void usage() {
        System.out.println("Usage : ServerChatFusion name");
    }

}

