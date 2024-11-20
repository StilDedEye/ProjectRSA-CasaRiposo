package com.drimtim.projectrsacasariposo.MAIN_client;

import com.almasb.fxgl.net.Client;
import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class ControllerClientSelectionItem {

    @FXML
    public Text textClientUsername;
    @FXML
    public HBox hBoxClientItem;

    @FXML
    public void initialize () {
        hBoxClientItem.setOnMouseClicked(mouseEvent -> {
            String destinationUsername = textClientUsername.getText();
            String command = ":requestPublicKey:" + destinationUsername;

            System.out.println("DESTINAZIONE MARE USERNAME: " + destinationUsername);
            ClientSocket.instance.sendMessageToServer(command);
            ClientSocket.instance.receiverUsername = destinationUsername;
            System.err.println(ClientSocket.instance.receiverUsername);

            FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/clientChat.fxml"));
            try {
                GridPane gridPane = fxmlLoader.load();
                Scene scene = new Scene(gridPane);

                MainClient.primaryStage.setScene(scene);
                MainClient.currentScene=scene;
                MainClient.primaryStage.setResizable(true);
                ((ControllerChatClient)fxmlLoader.getController()).loadPreviousMessages();
            } catch (IOException e) {throw new RuntimeException(e);}
        });
    }



}
