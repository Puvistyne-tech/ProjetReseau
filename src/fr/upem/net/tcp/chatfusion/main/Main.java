package fr.upem.net.tcp.chatfusion.main;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.packet.StringPacket;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {

    public static Charset CHA = StandardCharsets.UTF_8;
    static ByteBuffer buffer = ByteBuffer.allocate(1024);

    public static void doit() {
        var t = CHA.encode("hello");
        var t2 = CHA.encode("123").flip();

        var buffer = new Buffer.Builder(OPCODE.FUSION_INIT_FWD)
                .addString("abcdef")
                .build();

        System.out.println(buffer);
    }

    public static void test2() {
        var tmpBuff = ByteBuffer.allocate(1024);
        String[] members = {"a", "a", "a"};
        for (var mem : members) {
            var tmp = new StringPacket(mem).toByteBuffer();
            tmpBuff.put(tmp);
        }

        var buffer = new Buffer.Builder()
                .addBuffer(tmpBuff)
                .build();

        System.out.println(buffer);
    }


    public static void main(String[] args) {
        System.out.println("Just for testing");


        doit();
        test2();
    }
}
