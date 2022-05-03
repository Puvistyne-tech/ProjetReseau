package fr.upem.net.tcp.chatfusion.client;

import java.nio.channels.SelectionKey;

public record Client(String name, SelectionKey key) {

    @Override
    public String name() {
        return name;
    }

    @Override
    public SelectionKey key() {
        return key;
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", key=" + key +
                '}';
    }
}
