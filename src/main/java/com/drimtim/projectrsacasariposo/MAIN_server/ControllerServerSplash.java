package com.drimtim.projectrsacasariposo.MAIN_server;

import com.drimtim.projectrsacasariposo.sockets.ServSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
        textAddress.setText(InetAddress.getLocalHost().getHostAddress() +":1201");
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
        Thread threadListeningServer = new Thread(()->{
            try {
                ServSocket servSocket = new ServSocket();
                servSocket.initializeListening();
            } catch (IOException e) {throw new RuntimeException(e);}
        });
        threadListeningServer.setName("threadListeningServer");
        threadListeningServer.start();
    }
}