package fr.upem.net.tcp.chatfusion.client;

import java.nio.channels.SelectionKey;

public record Client(String name, SelectionKey key) {

    /**
     * Gets the client name
     * @return the client name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Gets the client key
     * @return the key
     */
    @Override
    public SelectionKey key() {
        return key;
    }

    /**
     * Create a string representation of a client.
     * @return a string
     */
    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", key=" + key +
                '}';
    }
}
