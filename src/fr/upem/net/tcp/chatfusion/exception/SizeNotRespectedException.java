package fr.upem.net.tcp.chatfusion.exception;

import java.io.IOException;

public class SizeNotRespectedException extends IOException {
    public SizeNotRespectedException(int size) {
        super("The size should not be more than " + size);
    }
}
