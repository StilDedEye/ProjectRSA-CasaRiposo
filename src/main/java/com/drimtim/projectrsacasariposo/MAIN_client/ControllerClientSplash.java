package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

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
    public Text textErrorMessage;


    @FXML
    public void initialize () throws UnknownHostException {
        textClientName.setText(InetAddress.getLocalHost().getHostName());
    }

    @FXML
    public void onStartClicked () throws Exception {
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

        // connect to server
        try {
            if (ClientSocket.instance.configureServerSocket()) {
                imageViewStatus.setImage(new Image(getClass().getResourceAsStream("/com/drimtim/projectrsacasariposo/other/interface/gifs/loading.gif")));

                FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/clientChatSelection.fxml"));
                BorderPane borderPane = fxmlLoader.load();
                fxmlLoader.getController();
                Scene scene = new Scene(borderPane);

                MainClient.primaryStage.setTitle(":client-" + username);
                MainClient.primaryStage.setScene(scene);
                MainClient.primaryStage.setResizable(true);
            } else {
                System.err.println("ERROR -> CONNECTIONREFUSED| username already taken");

                // put an error image
                imageViewStatus.setImage(new Image(getClass().getResourceAsStream("/com/drimtim/projectrsacasariposo/other/interface/gifs/dinousaurGifError.gif")));
                textErrorMessage.setOpacity(1.0);
                textErrorMessage.setText("Username already taken");
            }
        } catch (Exception e) {
            // put an error image
            imageViewStatus.setImage(new Image(getClass().getResourceAsStream("/com/drimtim/projectrsacasariposo/other/interface/gifs/dinousaurGifError.gif")));
            textErrorMessage.setOpacity(1.0);
            textErrorMessage.setText("An error occured.");
        }

    }

    private void resetGUIAfterAnError () {
        textErrorMessage.setOpacity(1.0);
        imageViewStatus.setOpacity(0.0);
        txtUsername.setText("");
    }
}
