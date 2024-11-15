package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ControllerClientSplash {

    @FXML
    public Button btnAvvia;
    @FXML
    public ImageView imageViewStatus;
    @FXML
    public Text textClientName;
    @FXML
    public TextField txtIpPort;
    @FXML
    public TextField txtUsername;


    @FXML
    public void initialize () throws UnknownHostException {
        textClientName.setText(InetAddress.getLocalHost().getHostName());
    }

    @FXML
    public void onStartClicked () throws IOException {
        String username = txtUsername.getText();
        String ipPort = txtIpPort.getText();

        String ip = ipPort.substring(0, ipPort.indexOf(":"));
        String port = ipPort.substring(ipPort.indexOf(":")+1);

        int finalPort;
        try {
            finalPort = Integer.parseInt(port);
        } catch (Exception e) {
            System.err.println("PORTA " + port + " NON VALIDA - CONNECTION REFUSED");
            return;   // the port is not valid
        }

        // set username, ip and port of client
        ClientSocket.instance.setUsername(username);
        ClientSocket.instance.setServerPort(finalPort);
        ClientSocket.instance.setServerIp(ip);

        imageViewStatus.setOpacity(1.0); // make the loading gif visible
        btnAvvia.setOpacity(0.0);


        // connect to server
        ClientSocket.instance.configureServerSocket();
    }
}
