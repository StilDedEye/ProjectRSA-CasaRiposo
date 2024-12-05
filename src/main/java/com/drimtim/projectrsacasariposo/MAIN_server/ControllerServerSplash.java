package com.drimtim.projectrsacasariposo.MAIN_server;

import com.drimtim.projectrsacasariposo.sockets.ServSocket;
import com.drimtim.projectrsacasariposo.sockets.Utilities.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class ControllerServerSplash {
    @FXML
    public Text textAddressLocal;
    @FXML
    public Text textAddressPublic;
    @FXML
    public Button btnAvvia;
    @FXML
    public Text textServerName;
    @FXML
    public ImageView imgViewCopyLocal;
    @FXML
    public ImageView imgViewCopyPublic;


    @FXML
    public void initialize () throws UnknownHostException {
        textAddressLocal.setText(" Local "+Inet4Address.getLocalHost().getHostAddress()+":"+ServSocket.port);
        textAddressPublic.setText(" Public "+getPublicIP()+":"+ServSocket.port);
        textServerName.setText(InetAddress.getLocalHost().getHostName());
    }

    @FXML
    protected void onCopyClickedLocal() throws Exception {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(InetAddress.getLocalHost().getHostAddress() + ":" +ServSocket.port), null);

        new Thread(()->{
            try {
                String a = textAddressLocal.getText();
                Paint p = textAddressLocal.getFill();
                textAddressLocal.setText("Copied");
                textAddressLocal.setFill(Paint.valueOf("#05993e"));
                Thread.sleep(1000);
                textAddressLocal.setText(a);
                textAddressLocal.setFill(p);
            } catch (Exception ignored) {}
        }).start();
    }

    @FXML
    protected void onCopyClickedPublic() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getPublicIP()+":"+ServSocket.port), null);
        new Thread(()->{
            try {
                String a = textAddressPublic.getText();
                Paint p = textAddressPublic.getFill();
                textAddressPublic.setText("Copied");
                textAddressPublic.setFill(Paint.valueOf("#05993e"));
                Thread.sleep(1000);
                textAddressPublic.setText(a);
                textAddressPublic.setFill(p);
            } catch (Exception ignored) {}
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
                new ServSocket();
            } catch (IOException ignored) {}
        });
        threadListeningServer.setName("threadListeningServer");
        threadListeningServer.start();
    }

    public static String getPublicIP() {
        String url = "https://checkip.amazonaws.com"; // Servizio per ottenere l'indirizzo pubblico
        try {
            // Creazione della connessione
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Lettura della risposta
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIP = reader.readLine();
            reader.close();

            return publicIP;
        } catch (Exception e) {
            Logger.log(e.getMessage(), Logger.EXCEPTION);
            return "####.####.####.####";
        }
    }
}