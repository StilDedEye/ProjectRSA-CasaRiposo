package com.drimtim.projectrsacasariposo.MAIN_server;

import com.drimtim.projectrsacasariposo.MAIN_client.ControllerClientSplash;
import com.drimtim.projectrsacasariposo.MAIN_client.MainClient;
import com.drimtim.projectrsacasariposo.sockets.ServSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class ControllerServerSplash {
    @FXML
    public Text textAddress;
    @FXML
    public Button btnAvvia;
    @FXML
    public Text textServerName;
    @FXML
    public ImageView imgViewCopy;


    @FXML
    public void initialize () throws UnknownHostException {
        textAddress.setText(Inet4Address.getLocalHost().getHostAddress()+":3000");
        textServerName.setText(InetAddress.getLocalHost().getHostName());
    }

    @FXML
    protected void onCopyClicked () throws Exception {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(InetAddress.getLocalHost().getHostAddress() +":1201"), null);


        new Thread(()->{
            try {
                String a = textAddress.getText();
                Paint p = textAddress.getFill();
                textAddress.setText("Copied");
                textAddress.setFill(Paint.valueOf("#05993e"));
                Thread.sleep(2000);
                textAddress.setText(a);
                textAddress.setFill(p);
            } catch (Exception e) {}
        }).start();
    }

    @FXML
    public void onStartClicked () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/drimtim/projectrsacasariposo/server/serverConsole.fxml"));
        StackPane stackPane = fxmlLoader.load();
        ((ControllerServerConsole)fxmlLoader.getController()).configureTxa();
        Scene scene = new Scene(stackPane);

        MainServer.primaryStage.setScene(scene);
        MainServer.currentScene=scene;

        Thread threadListeningServer = new Thread(() -> {
            try {
                ServSocket servSocket = new ServSocket();
                servSocket.initializeListening();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        threadListeningServer.setName("threadListeningServer");
        threadListeningServer.start();
    }
}