package it.michele.filereciever;

import it.michele.netty.Client;
import it.michele.netty.packets.server.SPacketSend;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class Controller {

    public TextField title;
    public TextArea body;
    public Button send;

    public void initialize(){
        send.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            for(Client client : ServerHandler.clients.values()){
                client.getCtx().writeAndFlush(new SPacketSend(title.getText(), body.getText()));
            }
        });
    }
}
