package fr.upem.net.tcp.chatfusion.context;

import fr.upem.net.tcp.chatfusion.packet.Packet;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public interface IContext {

    int BUFFER_SIZE = 1_024;
    Charset CHARSET = StandardCharsets.UTF_8;

    void processIn() throws Exception;
    void processOut();
    void queueMessage(Packet packet);
    void updateInterestOps();
    void doRead() throws Exception;
    void doWrite() throws IOException;
    void silentlyClose();
    SelectionKey getKey();

}
