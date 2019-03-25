package it.michele.filereciever;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import it.michele.netty.NettyNetworkHandler;
import it.michele.netty.PacketDecoder;
import it.michele.netty.PacketEncoder;
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

public class Main extends Application {

    public Stage stage;

    private static ServerHandler networkHandler = new ServerHandler();

    public static void main(String... args){
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try{
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup);
                bootstrap.channel(NioServerSocketChannel.class);
                bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
                bootstrap.option(ChannelOption.SO_BACKLOG, 2);
                bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new NettyNetworkHandler(networkHandler));
                    }
                });

                System.out.println("Server Started");
                ChannelFuture future = bootstrap.bind(20604).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();

        launch();
    }

    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();
        FXMLLoader loader = new FXMLLoader(getView("page"));
        Parent parent = loader.load();

        Scene scene = new Scene(root,361, 104);

        Controller controller = loader.getController();

        stage.setTitle("FileReceiver");
        stage.getIcons().add(new Image(getResourceAsStream("images/icon.png")));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
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
                System.exit(0);
            }

            ActionListener closeListener = (e) -> {
                Platform.exit();
                System.exit(0);
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
                System.exit(0);
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
