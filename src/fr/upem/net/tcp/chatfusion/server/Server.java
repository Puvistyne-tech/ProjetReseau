package fr.upem.net.tcp.chatfusion.server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

public record Server(String name, InetSocketAddress address, SelectionKey key) {
}
