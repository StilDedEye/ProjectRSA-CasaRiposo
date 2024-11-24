package com.drimtim.projectrsacasariposo.MAIN_server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

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
