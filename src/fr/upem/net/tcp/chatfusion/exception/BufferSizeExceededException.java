package fr.upem.net.tcp.chatfusion.exception;

public class BufferSizeExceededException extends RuntimeException{
    public BufferSizeExceededException() {
        super("Buffer size is limited to 1024");
    }
}
