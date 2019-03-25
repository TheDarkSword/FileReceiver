package it.michele.filereciever;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import it.michele.netty.NettyNetworkHandler;
import it.michele.netty.PacketDecoder;
import it.michele.netty.PacketEncoder;
import it.michele.netty.packets.client.CPacketLogout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class Main extends Application {

    public static final UUID ID = UUID.randomUUID();
    public static Controller controller;

    public Stage stage;

    public static void main(String... args){
        if(args.length == 0) System.exit(1);
        ClientHandler networkHandler = new ClientHandler();

        new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try{
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel channel){
                        channel.pipeline().addLast(new PacketEncoder(), new PacketDecoder(), new NettyNetworkHandler(networkHandler));
                    }
                });

                System.out.println("Connecting...");
                ChannelFuture future = bootstrap.connect(args[0], 20604).sync();
                future.channel().closeFuture().sync();

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();

        launch();
    }

    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();
        FXMLLoader loader = new FXMLLoader(getView("page"));
        Parent parent = loader.load();

        Scene scene = new Scene(root,361, 78);

        controller = loader.getController();

        stage.setTitle("FileReceiver");
        stage.getIcons().add(new Image(getResourceAsStream("images/icon.png")));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            ClientHandler.ctx.writeAndFlush(new CPacketLogout(Main.ID));
            //System.exit(0);
        });

        stage.show();

        root.setCenter(parent);
        this.stage = stage;
        postInit();
    }

    private void postInit(){
        Platform.setImplicitExit(false);
        this.createTrayIcon();
    }

    private void createTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage icon = null;

            try {
                icon = ImageIO.read(getResourceAsStream("images/tryicon.png"));
            } catch (IOException e) {
                e.printStackTrace();
                ClientHandler.ctx.writeAndFlush(new CPacketLogout(Main.ID));
                return;
                //System.exit(0);
            }

            ActionListener closeListener = (e) -> {
                Platform.exit();
                ClientHandler.ctx.writeAndFlush(new CPacketLogout(Main.ID));
                //System.exit(0);
            };
            ActionListener showListener = (e) -> Platform.runLater(() -> stage.show());
            PopupMenu menu = new PopupMenu();
            MenuItem showItem = new MenuItem("Apri");
            showItem.addActionListener(showListener);
            menu.add(showItem);
            MenuItem closeItem = new MenuItem("Chiudi");
            closeItem.addActionListener(closeListener);
            menu.add(closeItem);
            TrayIcon trayIcon = new TrayIcon(icon, "FileReceiver", menu);
            trayIcon.addActionListener(showListener);
            stage.setOnCloseRequest((event) -> this.hide());

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }

        }
    }

    private void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
            } else {
                //System.exit(0);
                ClientHandler.ctx.writeAndFlush(new CPacketLogout(Main.ID));
            }

        });
    }

    public InputStream getResourceAsStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    public URL getResource(String path) {
        return this.getClass().getClassLoader().getResource(path);
    }

    public URL getView(String name) {
        return this.getResource("view/" + name + ".fxml");
    }
}
