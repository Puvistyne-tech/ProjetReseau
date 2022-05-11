package fr.upem.net.tcp.chatfusion.exception;

public class UnknownInputReceivedException extends RuntimeException {
    public UnknownInputReceivedException() {
        super("Unknown commands received. Ignoring...\nTry again");
    }
}
