package fr.upem.net.tcp.chatfusion.packet;

import fr.upem.net.tcp.chatfusion.buffer.Buffer;
import fr.upem.net.tcp.chatfusion.utils.OPCODE;
import fr.upem.net.tcp.chatfusion.visitor.IPacketVisitor;

import java.nio.ByteBuffer;

public class LoginAnonymousPacket implements Packet {

    private final String login;
    private final OPCODE opcode;

    public LoginAnonymousPacket(String login) {
        this.login = Packet.verifySize(login, 30);
        this.opcode = OPCODE.LOGIN_ANONYMOUS;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return new Buffer.Builder()
                .addOpCode(opcode)
                .addString(login)
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
