package fr.upem.net.tcp.chatfusion.server;

import fr.upem.net.tcp.chatfusion.client.Client;
import fr.upem.net.tcp.chatfusion.packet.FusionInitPacket;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.Helpers;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private String leader;
    private final String name;
    private final ServerSocketChannel serverSocketChannel;

    private final HashMap<String, SocketAddress> servers = new HashMap<>();
    private final List<String> serverList = new ArrayList<>();

    private final Selector selector;

    private Thread console;
    private Thread fusion;
    private final ArrayBlockingQueue<Commands> commandQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(10);


    private final Map<String, SocketAddress> clients = new HashMap<String, SocketAddress>();
    private final List<Client> clientList =new ArrayList<>();

    private final Object lock = new Object();

    public String getServerName() {
        return this.name;
    }

    public String getLeaderName() {
        return this.leader;
    }

    public Server(String name) throws IOException {
        this.name = Objects.requireNonNull(name);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(7777));
//        serverSocketChannel.bind(null);

        System.out.println(serverSocketChannel.getLocalAddress());
        selector = Selector.open();
        servers.put(name, serverSocketChannel.getLocalAddress());

        this.leader = name;
        this.console = new Thread(this::consoleRun);
        this.fusion = new Thread(this::fusionRun);
    }

    private void fusionRun() {
        //console.interrupt();
        System.out.println("entrez server name port");
        var scanner = new Scanner(System.in);
//        if (scanner.hasNextLine()) {
//            var severname = scanner.nextLine();
            var severname = "localhost";
//            if (scanner.hasNextLine()) {
//                var address = scanner.nextLine();
                var address = "62709";
                var p = new FusionInitPacket(severname, new InetSocketAddress(Integer.parseInt(address)), serverList.size(), serverList);
                if (queue.isEmpty()){
                    try {
//                        serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(address)));

                        queue.put(p);
                        selector.wakeup();
                        selector.keys().forEach(oneKey -> {
                            var context = (Context) oneKey.attachment();
                            var client = oneKey.channel();
                            if (client == serverSocketChannel) {
                                System.out.println(client);
                                oneKey.interestOps(SelectionKey.OP_WRITE);
//                                count.getAndIncrement();
                            }
                        });
//                        updateInterestOps();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
            }

//        }
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
            case MERGE -> {
                console.interrupt();
                fusion.start();
            }
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

        this.clients.put(login, address);
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

    public boolean ifClientAlreadyConnected(String client) {
        System.out.println(clients);
        var t = clients.containsKey(client);
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

//    public SocketAddress getClientAddress(String clientName){
//        var t=clients.get(clientName);
//        SelectionKey res=null;
//        for (var oneKey:selector.keys()){
////        this.selector.keys().stream().forEach(oneKey->{
//            var s=(SocketChannel)oneKey.channel();
//            try {
//                if (s.getRemoteAddress()==t){
//                    SelectionKey oneKey1 = oneKey;
//                    res= oneKey1;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
////        );
//        return clients.get(clientName);
//    }
    public SelectionKey getClientAddress(String clientName){
        var t=clients.get(clientName);
        SelectionKey res=null;
        for (var oneKey:selector.keys()){
            var context = (Context) oneKey.attachment();
            var client = oneKey.channel();
            if (client != serverSocketChannel && !context.isClosed()&&context.login.equals(clientName)) {
                System.out.println(context);
                res=oneKey;
            }
        }
        return res;
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

            if (context != null) {
                (context).queueMessage(packet);
            }


        });
    }

    public void sendTo(SelectionKey key, Packet packet) {
        var context = (Context) key.attachment();

        if (context != null)
            (context).queueMessage(packet);
    }

    public void sendTo(SocketAddress address, Packet packet) {
//        var context = (Context) key.attachment();
//
//        if (context != null)
//            (context).queueMessage(packet);
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1) {
            usage();
            return;
        }

        new Server(args[0]).launch();
    }

    private static void usage() {
        System.out.println("Usage : Server name");
    }

}

