package fr.upem.net.tcp.chatfusion.client;

import fr.upem.net.tcp.chatfusion.reader.PublicMessageReader;
import fr.upem.net.tcp.chatfusion.reader.Reader;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler {
    public void publicMessageHandler(PublicMessageReader reader, ByteBuffer buffer){
        assert reader != null;
        Reader.ProcessStatus status = reader.process(buffer);
        switch (status) {
            case DONE:
                var value = reader.get();
                var dtf = DateTimeFormatter.ofPattern("HH:mm");
                System.out.println("("+value.server() +" : "+value.login()+")"+dtf.format(LocalDateTime.now()) + " ::: " + value.message());
                reader.reset();
                break;
            case REFILL:
                return;
            case ERROR:
//                silentlyClose();
                return;
        }
    }
}
