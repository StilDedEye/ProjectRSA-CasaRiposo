package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ControllerChatClient {
    public static ControllerChatClient instance;
    @FXML
    private Button btnSendMessage;

    @FXML
    private TextField txtInputMessage;

    @FXML
    private VBox vBoxMessages;

    public ControllerChatClient() {
        instance = this;
    }

    @FXML
    public void initialize () {

    }

    @FXML
    protected void onSendClicked () {
        String message = txtInputMessage.getText();
        txtInputMessage.setText("");
        ClientSocket.instance.sendMessageToClient(message);
        addMessage(message, true);
    }

    public void addMessage (String message, boolean sentByMe) {
        HBox containerMessage = new HBox();
        containerMessage.setStyle("" +
                "-fx-padding: 5px 5px 5px 5px;");
        Label textToShow = new Label(message);


        if (sentByMe) {
            textToShow.setStyle("-fx-background-color: #669bbc;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-padding: 5px 5px 5px 5px;");
            containerMessage.getChildren().add(textToShow);
            containerMessage.setAlignment(Pos.CENTER_RIGHT);
            vBoxMessages.getChildren().add(containerMessage);
        } else {
            textToShow.setStyle("-fx-background-color: #2a9d8f;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-padding: 5px 5px 5px 5px;");
            containerMessage.getChildren().add(textToShow);
            containerMessage.setAlignment(Pos.BASELINE_LEFT);
            vBoxMessages.getChildren().add(containerMessage);
        }

    }

}
