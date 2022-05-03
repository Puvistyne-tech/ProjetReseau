package fr.upem.net.tcp.chatfusion.exception;

public class BadPacketReceivedException extends RuntimeException {
    public BadPacketReceivedException() {
        super("Unsupported packet Received. Ignoring...");
    }
}
