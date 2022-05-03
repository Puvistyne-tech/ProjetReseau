package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.packet.LoginAnonymousPacket;
import fr.upem.net.tcp.chatfusion.packet.LoginPasswordPacket;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.utils.Commander;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class ClientChatFusion {

    static private Logger logger = Logger.getLogger(ClientChatFusion.class.getName());

    private final SocketChannel sc;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private final String login;
    private final String password;
    private final boolean authentified;

    private Context uniqueContext;
    private SelectionKey key;

    private final Thread main;
    private final Thread console;
    private final Thread connectToServer;

    private final Object lock = new Object();

    private final BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(10);


    public ClientChatFusion(String login, InetSocketAddress serverAddress) throws IOException {
        this.login = login;
        this.serverAddress = serverAddress;
        this.password = null;
        this.authentified = false;

        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.main = null;
        this.console = new Thread(this::consoleRun);
        this.connectToServer = new Thread(this::connectToServer);
    }

    public ClientChatFusion(String login, String password, InetSocketAddress serverAddress) throws IOException {
        this.login = login;
        this.password = password;
        this.serverAddress = serverAddress;
        this.authentified = true;

        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.main = null;
        this.connectToServer = new Thread(this::connectToServer);
    }

    private void consoleRun() {
        try {
            try (var scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    var msg = scanner.nextLine();
                    sendCommand(msg);
                }
            }
            logger.info("Console thread stopping");
        } catch (InterruptedException | IOException e) {
            logger.info("Console thread has been interrupted");
        }
    }

    /**
     * Send instructions to the selector via a BlockingQueue and wake it up
     *
     * @param msg
     * @throws InterruptedException
     */

    private void sendCommand(String msg) throws InterruptedException, IOException {

        if (!queue.isEmpty()) return;

        synchronized (lock) {

            var t = Commander.commande(login,uniqueContext.getServerName(),msg);
            System.out.println(t.toString());
            queue.put(t);

            selector.wakeup(); // Force le selector à sortir de selectojkr.select, pour pouvoir effectuer l'interaction correctement entre la thread console et la thread main
        }
    }

    /**
     * Processes the command from the BlockingQueue
     */

    private void processCommands() {

        while (!queue.isEmpty()) {
            var packet = queue.poll();
            //TODO Packets
            uniqueContext.queueMessage(packet);
        }

    }

    public void connectToServer() {
        try {
            if (authentified) {
                queue.put(new LoginPasswordPacket(login, password));
            }

            queue.put(new LoginAnonymousPacket(login));

            selector.wakeup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void initiate() throws IOException {
        sc.configureBlocking(false);
        key = sc.register(selector, SelectionKey.OP_CONNECT);
        sc.connect(serverAddress);
        uniqueContext = new Context(key);
        key.attach(uniqueContext);
        logger.info("Connection Established to : " + this.serverAddress + "\nWaiting for the Authentification");
    }

    public void disconnect() {
        connectToServer.interrupt();
        console.interrupt();
        main.interrupt();
    }

    public void launch() throws IOException {

        initiate();

        connectToServer.start();
        //TODO


        console.start();
        while (!Thread.interrupted()) {
            try {
                selector.select(this::treatKey);
                processCommands();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
    }

    private void treatKey(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                uniqueContext.doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                uniqueContext.doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                uniqueContext.doRead();
            }
        } catch (IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = (Channel) key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    public static void main(String[] args) throws NumberFormatException, IOException {

        if (args.length != 3) {
            usage();
            return;
        }

        var login = Commander.getLogin(args);
        var server = new InetSocketAddress(login[0], Integer.parseInt(login[1]));

        if (login.length == 4) {
            var pass = login[2];
            new ClientChatFusion(args[0], pass, server).launch();
        } else {
            new ClientChatFusion(args[0], server).launch();
        }

    }

    private static void usage() {
        System.out.println("Usage : ClientChatFusion login hostname port");
    }
}
