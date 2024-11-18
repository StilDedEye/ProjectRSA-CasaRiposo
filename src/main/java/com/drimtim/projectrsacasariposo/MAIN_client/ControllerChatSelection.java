package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ControllerChatSelection {
    public static ControllerChatSelection instance;

    @FXML
    private VBox vBoxChats;

    public ControllerChatSelection() {
        instance = this;
    }

    @FXML
    public void initialize () {
        updateVboxChats();
    }

    public void updateVboxChats () {
        if (!vBoxChats.getChildren().isEmpty())
            vBoxChats.getChildren().clear();

        Platform.runLater(()->{
            for (String username: ClientSocket.instance.connectedClients) {
                FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/cllientSelectionItem.fxml"));
                try {
                    HBox hbox = fxmlLoader.load();
                    ControllerClientSelectionItem c = fxmlLoader.getController();
                    c.textClientUsername.setText(username);
                    vBoxChats.getChildren().add(hbox);
                } catch (IOException e) {throw new RuntimeException(e);}

            }
        });
    }
}
