package it.michele.filereciever;

import io.netty.channel.ChannelHandlerContext;
import it.michele.netty.NetworkHandler;
import it.michele.netty.packets.Packet;
import it.michele.netty.packets.client.CPacketHandshake;
import it.michele.netty.packets.client.CPacketLogin;
import it.michele.netty.packets.server.SPacketHandshake;
import it.michele.netty.packets.server.SPacketLogin;
import it.michele.netty.packets.server.SPacketLogout;
import it.michele.netty.packets.server.SPacketSend;

import java.io.*;
import java.nio.file.Paths;

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
public class ClientHandler implements NetworkHandler {
    public static ChannelHandlerContext ctx;

    private File dataFolder = new File(Paths.get("").toAbsolutePath().toString());

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        ctx.writeAndFlush(new CPacketHandshake(1));
    }

    @Override
    public void processHandshake(Packet packet, ChannelHandlerContext ctx){
        SPacketHandshake in = (SPacketHandshake) packet;

        if(!in.isConnectionAllowed()){
            System.err.println("Errore, Versione Obsoleta");
            return;
        }

        ctx.writeAndFlush(new CPacketLogin(Main.ID, 1));
    }

    @Override
    public void processLogin(Packet packet, ChannelHandlerContext ctx){
        SPacketLogin in = (SPacketLogin) packet;

        if(!in.isLoginSuccessful()){
            System.err.println("Login Error, Server refused");
            return;
        }

        ClientHandler.ctx = ctx;

        System.out.println("Login successful!");
    }

    @Override
    public void processLogout(Packet packet, ChannelHandlerContext ctx){
        SPacketLogout in = (SPacketLogout) packet;

        if(in.isLogoutSuccessful()) System.exit(1);
    }

    @Override
    public void processReceive(Packet packet, ChannelHandlerContext ctx) {

    }

    @Override
    public void processSend(Packet packet, ChannelHandlerContext ctx) {
        SPacketSend in = (SPacketSend) packet;

        Main.controller.body.appendText("Recezione classe " + in.getTitle() + "\n");

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(in.getTitle() + ".java")));
            writer.write(in.getBody());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
