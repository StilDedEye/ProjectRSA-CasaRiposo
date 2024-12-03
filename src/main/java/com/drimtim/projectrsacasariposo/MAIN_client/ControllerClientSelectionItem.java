package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import com.drimtim.projectrsacasariposo.sockets.Utilities.Utilities;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ControllerClientSelectionItem {

    @FXML
    public Text textClientUsername;
    @FXML
    public HBox hBoxClientItem;

    @FXML
    public ImageView imgViewProfilePic;

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
                MainClient.primaryStage.setResizable(false);
                ((ControllerChatClient)fxmlLoader.getController()).loadPreviousMessages();
            } catch (IOException e) {throw new RuntimeException(e);}
        });

        // configurazione generale barra
        Platform.runLater(()->{
            // selezione immagine casuale da quelle di default
            int picNumber = Utilities.getNumberFromUsername(textClientUsername.getText());
            Image profilePic = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/drimtim/projectrsacasariposo/other/interface/defaultProfilePic/pic" + picNumber + ".png")));
            List<Color> colorList = Utilities.getDominantColors(profilePic, 2);

            if (picNumber==2) {
                hBoxClientItem.setStyle(hBoxClientItem.getStyle()+"-fx-background-color: linear-gradient(from 50% 50% to 100% 100%, "+Utilities.colorToHex(colorList.getFirst())+", "+Utilities.colorToHex(colorList.getLast())+");");
            } else
                hBoxClientItem.setStyle(hBoxClientItem.getStyle()+"-fx-background-color: linear-gradient(from 50% 50% to 100% 100%, "+Utilities.colorToHex(colorList.getFirst())+", "+Utilities.colorToHex(colorList.getLast())+");");


            imgViewProfilePic.setImage(profilePic);
        });
    }




}
