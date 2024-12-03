package com.drimtim.projectrsacasariposo.MAIN_server;

import com.drimtim.projectrsacasariposo.sockets.ServSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class ControllerServerConsole {
    public static ControllerServerConsole instance;

    @FXML
    private Button btnStopServer;

    @FXML
    private ScrollPane scrllPaneConsole;

    @FXML
    private TextArea txaServerConsole;

    @FXML
    public void initialize ()  {
        instance = this;

        btnStopServer.setOnAction(actionEvent -> {
            try {
                ServSocket.instance.closeServer();
            } catch (IOException e) {throw new RuntimeException(e);}
            System.exit(0);
        });
    }

    public void configureTxa () {
        txaServerConsole.heightProperty().addListener((observable, oldValue, newValue) -> {
            double scrollPosition = newValue.doubleValue() / scrllPaneConsole.getContent().getBoundsInParent().getHeight();
            scrllPaneConsole.setVvalue(scrollPosition);
        });
    }


    public void log (String text) {
        txaServerConsole.appendText(text + "\n");
    }
}
