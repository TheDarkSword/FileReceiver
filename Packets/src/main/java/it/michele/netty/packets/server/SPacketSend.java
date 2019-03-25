package it.michele.netty.packets.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import it.michele.netty.NetworkHandler;
import it.michele.netty.packets.Packet;
import it.michele.netty.packets.PacketEnum;

import java.nio.charset.Charset;

public class SPacketSend implements Packet {
    private PacketEnum type = PacketEnum.S_PACKET_SEND;

    private String title;
    private String body;

    public SPacketSend(){

    }

    public SPacketSend(String title, String body){
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    @Override
    public void processPacket(ChannelHandlerContext ctx, NetworkHandler handler){
        handler.processSend(this, ctx);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out){
        byte[] title = this.title.getBytes();
        byte[] body = this.body.getBytes();

        out.writeInt(type.getId());
        out.writeInt(title.length);
        out.writeBytes(title);
        out.writeInt(body.length);
        out.writeBytes(body);
    }

    @Override
    public Packet decode(ByteBuf buf){
        Charset charset = Charset.forName("UTF-8");
        int titleLen = buf.readInt();
        title = buf.readCharSequence(titleLen, charset).toString();
        int bodyLen = buf.readInt();
        body = buf.readCharSequence(bodyLen, charset).toString();

        return this;
    }
}
