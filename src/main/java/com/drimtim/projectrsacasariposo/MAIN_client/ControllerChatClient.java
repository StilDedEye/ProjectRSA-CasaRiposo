package com.drimtim.projectrsacasariposo.MAIN_client;

import com.almasb.fxgl.net.Client;
import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ControllerChatClient {
    @FXML
    private Button btnSendMessage;

    @FXML
    private TextField txtInputMessage;

    @FXML
    private VBox vBoxMessages;

    @FXML
    public void initialize () {

    }

    @FXML
    protected void onSendClicked () {
        String message = txtInputMessage.getText();
        txtInputMessage.setText("");
        ClientSocket.instance.sendMessage(message);
    }

}
