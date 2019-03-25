package it.michele.filereciever;

import io.netty.channel.ChannelHandlerContext;
import it.michele.netty.Client;
import it.michele.netty.NetworkHandler;
import it.michele.netty.packets.Packet;
import it.michele.netty.packets.client.CPacketHandshake;
import it.michele.netty.packets.client.CPacketLogin;
import it.michele.netty.packets.client.CPacketLogout;
import it.michele.netty.packets.server.SPacketHandshake;
import it.michele.netty.packets.server.SPacketLogin;
import it.michele.netty.packets.server.SPacketLogout;

import java.util.HashMap;
import java.util.UUID;

/**
 * Copyright © 2019 by Michele Giacalone
 * This file is part of OnlineLobby.
 * OnlineLobby is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class ServerHandler implements NetworkHandler {
    public static HashMap<UUID, Client> clients = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx){

    }

    @Override
    public void processHandshake(Packet packet, ChannelHandlerContext ctx){
        CPacketHandshake in = (CPacketHandshake) packet;

        boolean response = false;
        if(in.getProtocolVersion() == 1){
            response = true;
        }

        SPacketHandshake out = new SPacketHandshake(response);
        ctx.writeAndFlush(out);
    }

    @Override
    public void processLogin(Packet packet, ChannelHandlerContext ctx){
        CPacketLogin in = (CPacketLogin) packet;

        if(clients.containsKey(in.getUuid())){
            ctx.writeAndFlush(new SPacketLogin(false));
            return;
        }

        clients.put(in.getUuid(), new Client(in.getUuid(), ctx));
        ctx.writeAndFlush(new SPacketLogin(true));
    }

    @Override
    public void processLogout(Packet packet, ChannelHandlerContext ctx){
        CPacketLogout in = (CPacketLogout) packet;

        clients.remove(in.getUuid());
        ctx.writeAndFlush(new SPacketLogout(true));
        System.out.println(clients.size());
    }

    @Override
    public void processReceive(Packet packet, ChannelHandlerContext ctx) {
        System.out.println("Receive");
    }

    @Override
    public void processSend(Packet packet, ChannelHandlerContext ctx) {
        System.out.println("Send");
    }
}
