package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.exception.UnknownInputReceivedException;
import fr.upem.net.tcp.chatfusion.packet.LoginAnonymousPacket;
import fr.upem.net.tcp.chatfusion.packet.LoginPasswordPacket;
import fr.upem.net.tcp.chatfusion.packet.Packet;
import fr.upem.net.tcp.chatfusion.packet.PublicMessagePacket;
import fr.upem.net.tcp.chatfusion.utils.Commander;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * <p>
 * Represent a client.
 * </p>
 * <p>
 * This class contains informations that allow
 * the client to communicate with a remote
 * server.
 * </p>
 */
public class ClientChatFusion {

    static private Logger logger = Logger.getLogger(ClientChatFusion.class.getName());

    static private Path FILE_PATH;

    private final SocketChannel sc;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private final String login;
    private final String password;

    /**
     * Gets the current context
     *
     * @return the context
     */
    public Context getUniqueContext() {
        return uniqueContext;
    }

    private SelectionKey key;
    private Context uniqueContext;
    private boolean authenticated = false;


    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Packet> fileQueue = new ArrayBlockingQueue<>(20);


    //Threads
    private final Thread console;
    private final Thread connectToServer;

    private final Object lock = new Object();

    /**
     * Constructs a client according to a LOGIN_ANONYMOUS request
     *
     * @param host  the host name
     * @param port  the port
     * @param path  the folder's path where the file will be
     *              sent or received
     * @param login the login username
     * @throws IOException if an I/O error occurs
     */
    public ClientChatFusion(String host, String port, String path, String login) throws IOException {
        this.login = login;
        this.serverAddress = new InetSocketAddress(host, Integer.parseInt(port));
        this.password = null;
        FILE_PATH = Path.of(path);

        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.connectToServer = new Thread(this::connectToServer);
    }


    /**
     * Constructs a client according to a LOGIN_PASSWORD request
     *
     * @param host     the host name
     * @param port     the port
     * @param path     the folder's path where the file will be
     *                 sent or received
     * @param login    the login username
     * @param password the login password
     * @throws IOException if an I/O error occurs
     */
    public ClientChatFusion(String host, String port, String path, String login, String password) throws IOException {
        this.login = login;
        this.password = password;
        this.serverAddress = new InetSocketAddress(host, Integer.parseInt(port));
        FILE_PATH = Path.of(path);

        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.connectToServer = new Thread(this::connectToServer);
    }

    /**
     * Provides a client's command prompt
     */
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
     * Sends instructions to the selector via a BlockingQueue and wake it up
     *
     * @param msg the client input command
     * @throws InterruptedException if interrupted while waiting
     * @throws IOException          if an I/O error occurs
     */
    private void sendCommand(String msg) throws IOException, InterruptedException {

        if (!queue.isEmpty()) return;

        synchronized (lock) {
            queue.put(msg);
            selector.wakeup();
        }
    }




    /**
     * Processes the command from the BlockingQueue
     */
    private void processCommands() {

        while (!queue.isEmpty()) {
            var mgs = queue.poll();
            var who = Commander.readFromTerminal(mgs);
            if (who == OPCODE.FILE_PRIVATE) {
                var packets = Commander.treatPrivateFile(this, mgs);
                packets.forEach(packet -> {
                    System.out.println("<== Sending file ...");
                    this.uniqueContext.queueMessage(packet);
                    selector.wakeup();
                });
            } else if (who == OPCODE.MESSAGE_PRIVATE) {
                uniqueContext.queueMessage(
                        Commander.treatPrivateMessage(
                                uniqueContext.getServerName(),
                                getLogin(),
                                mgs
                        )
                );
            } else if (who == OPCODE.MESSAGE_PUBLIC) {
                uniqueContext.queueMessage(
                        new PublicMessagePacket(uniqueContext.getServerName(), getLogin(), mgs)
                );
            } else {
                throw new UnknownInputReceivedException();
            }
            selector.wakeup();
        }

    }

    /**
     * Instanciates and put into the BlockingQueue the proper packet
     * according to the command
     */
    public void connectToServer() {
        if (password != null) {
            uniqueContext.queueMessage(new LoginPasswordPacket(login, password));
        } else {
            uniqueContext.queueMessage(new LoginAnonymousPacket(login));
        }
        selector.wakeup();
    }

    /**
     * Initiates a connection with the remote server
     *
     * @throws IOException if an I/O error occurs
     */
    public void initiate() throws IOException {
        sc.configureBlocking(false);
        key = sc.register(selector, SelectionKey.OP_CONNECT);
        sc.connect(serverAddress);
        uniqueContext = new Context(this, key);
        key.attach(uniqueContext);
        logger.info("Connection Established to : " + this.serverAddress + "\nWaiting for the Authentification");
    }

    /**
     * Disconnects the client from the server
     */
    public void disconnect() {
        connectToServer.interrupt();
        console.interrupt();
    }

    /**
     * Launches the client
     *
     * @throws IOException if an I/O error occurs
     */
    public void launch() throws IOException {

        initiate();
        connectToServer.start();
        console.start();
        while (!Thread.interrupted()) {
            try {
                selector.select(this::treatKey);
                if (isLoggedIn()) {
                    processCommands();
                }
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
    }


    /**
     * Calls the proper method according to key's status
     *
     * @param key the client socket
     */
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

    /**
     * Close the client's channel
     *
     * @param key the client socket
     */
    private void silentlyClose(SelectionKey key) {
        Channel sc = (Channel) key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }


    /**
     * @throws NumberFormatException if a conversion is attempted from a bad format
     *                               string to a numeric type
     * @throws IOException           if an I/O error occurs
     */
    public static void main(String[] args) throws NumberFormatException, IOException {

        if (args.length != 4) {
            usage();
            return;
        }

        var res = Commander.getLogin(args);

        if (res.length == 4) {
            new ClientChatFusion(res[0], res[1], res[2], res[3]).launch();
        } else {
            new ClientChatFusion(res[0], res[1], res[2], res[3], res[4]).launch();
        }

    }

    /**
     * Displays help message in the command prompt
     */
    private static void usage() {
        System.out.println("Usage : hostname port File_Path Login");
    }

    /**
     * Get the login username
     *
     * @return the username
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the folder's path where the file will be
     * sent or received
     *
     * @return
     */
    public Path getPath() {
        return FILE_PATH;
    }

    /**
     * Gets if whether or not the client is still connected
     */
    public boolean isLoggedIn() {
        return this.authenticated;
    }

    /**
     * Sets the connection status to in progress
     */
    public void LogIn() {
        this.authenticated = true;
    }

    /**
     * Sets the connection status to closed
     */
    public void LogOut() {
        this.authenticated = false;
    }
}
