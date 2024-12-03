package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;

import java.io.IOException;

public class ControllerChatClient {
    public static ControllerChatClient instance;


    @FXML
    private Button btnSendMessage;
    @FXML
    private TextField txtInputMessage;
    @FXML
    private VBox vBoxMessages;
    @FXML
    public Button btnBackToChats;
    @FXML
    public ScrollPane scrollPaneVbox;

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

    @FXML
    protected void onBackToChats () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/clientChatSelection.fxml"));
        BorderPane borderPane = fxmlLoader.load();
        fxmlLoader.getController();
        Scene scene = new Scene(borderPane);

        MainClient.primaryStage.setScene(scene);
        MainClient.currentScene=scene;
        MainClient.primaryStage.setResizable(true);

        ClientSocket.instance.receiverUsername = null;
    }

    public void addMessage(String message, boolean sentByMe) {
        HBox containerMessage = new HBox();
        containerMessage.setStyle("-fx-padding: 5px 5px 5px 5px;");
        Label textToShow = new Label(message);
        //Setting the alignment to the label
        textToShow.setTextAlignment(TextAlignment.LEFT);
        textToShow.setWrapText(true);

        if (sentByMe) {
            textToShow.setStyle("-fx-background-color: #669bbc;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-padding: 5px 5px 5px 5px;");
            containerMessage.getChildren().add(textToShow);
            containerMessage.setAlignment(Pos.CENTER_RIGHT);
            SVGPath svgPath = new SVGPath();
            svgPath.setContent("M3 17h6V0c-.193 2.84-.876 5.767-2.05 8.782-.904 2.325-2.446 4.485-4.625 6.48A1 1 0 003 17z");
            svgPath.setFill(Color.web("#669bbc"));
            svgPath.setRotate(180);
            svgPath.setScaleX(1);
            containerMessage.getChildren().add(svgPath);
            HBox.setMargin(svgPath, new Insets(0,0,10.5,-3.5));
        } else {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent("M3 17h6V0c-.193 2.84-.876 5.767-2.05 8.782-.904 2.325-2.446 4.485-4.625 6.48A1 1 0 003 17z");
            svgPath.setFill(Color.web("#2a9d8f"));
            svgPath.setRotate(180);
            svgPath.setScaleX(-1);
            containerMessage.getChildren().add(svgPath);
            HBox.setMargin(svgPath, new Insets(-2,-3.5,12.5,0));
            textToShow.setStyle("-fx-background-color: #2a9d8f;" +
                    "-fx-background-radius: 5px 5px 5px 5px;" +
                    "-fx-padding: 5px 5px 5px 5px;");
            containerMessage.getChildren().add(textToShow);
            HBox.setMargin(textToShow, new Insets(3,0,0,0));
            containerMessage.setAlignment(Pos.BASELINE_LEFT);

        }
        vBoxMessages.getChildren().add(containerMessage);

        // listener sulla proprietà di altezza della VBox per aggiornare il valore di scorrimento
        vBoxMessages.heightProperty().addListener((observable, oldValue, newValue) -> {
            // calcola la posizione di scorrimento in base all'altezza della VBox
            double scrollPosition = newValue.doubleValue() / scrollPaneVbox.getContent().getBoundsInParent().getHeight();
            // imposta la posizione di scorrimento
            scrollPaneVbox.setVvalue(scrollPosition);
        });
        VBox.setVgrow(textToShow, Priority.NEVER);
        vBoxMessages.setMinHeight(Region.USE_PREF_SIZE); // Non comprimere la VBox
        vBoxMessages.setPrefHeight(Region.USE_COMPUTED_SIZE);
        vBoxMessages.setMaxHeight(Double.MAX_VALUE);
    }


    public void loadPreviousMessages () {

        // carica eventuali messaggi già inviati in precedenza
        if (ClientSocket.instance.checkIfUsernameExistsInsideMessages(ClientSocket.instance.receiverUsername)) {
            for (String message: ClientSocket.instance.allMessages.get(ClientSocket.instance.receiverUsername)) {
                if (message.substring(0, message.indexOf(":")).equals("sentByMe")) {
                    addMessage(message.substring(message.indexOf(":")+1), true);
                } else {
                    addMessage(message.substring(message.indexOf(":")+1), false);
                }
            }
        }

    }

}
