package com.drimtim.projectrsacasariposo.MAIN_client;

import com.drimtim.projectrsacasariposo.sockets.ClientSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ControllerClientSplash {

    @FXML
    public Button btnAvvia;
    @FXML
    public ImageView imageViewStatus;


    @FXML
    public void initialize () {

    }

    @FXML
    public void onStartClicked () {
        imageViewStatus.setOpacity(1.0); // make the loading gif visible
        btnAvvia.setOpacity(0.0);

    }
}
