package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
                vBoxChats.getChildren().add(new Label(username));
            }
        });
    }
}
