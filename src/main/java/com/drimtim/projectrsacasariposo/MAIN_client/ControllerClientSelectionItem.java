package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

            ClientSocket.instance.sendMessageToServer(command);


            FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/clientChat.fxml"));
            try {
                GridPane gridPane = fxmlLoader.load();
                fxmlLoader.getController();
                Scene scene = new Scene(gridPane);

                MainClient.primaryStage.setScene(scene);
                MainClient.primaryStage.setResizable(true);

            } catch (IOException e) {throw new RuntimeException(e);}
        });
    }



}
