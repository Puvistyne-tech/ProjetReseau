package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

/**
 * LOGIN_PASSWORD(1) =
 * 1 (OPCODE)
 * login (STRING<=30)
 * password (STRING<=30)
 */
public class LoginPasswordPacket implements Packet {
    private final OPCODE opcode;
    private final String login;
    private final String password;

    public LoginPasswordPacket(String login, String password) {
        this.opcode = OPCODE.LOGIN_PASSWORD;
        this.login = Packet.verifySize(login, 30);
        this.password = Packet.verifySize(password, 30);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder(opcode)
                .addString(login)
                .addString(password)
                .build();
    }
    @Override
    public void accept(IPacketVisitor visitor) {
        visitor.visit(this);
    }
    public String getLogin() {
        return this.login;
    }
}
