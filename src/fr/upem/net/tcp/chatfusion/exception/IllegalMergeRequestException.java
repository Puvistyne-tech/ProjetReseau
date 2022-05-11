package fr.upem.net.tcp.chatfusion.exception;

public class IllegalMergeRequestException extends RuntimeException {
    public IllegalMergeRequestException() {
        super("Unknown Server trying to merge. Ignoring...");
    }
}
