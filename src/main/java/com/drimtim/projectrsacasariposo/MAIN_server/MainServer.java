package com.drimtim.projectrsacasariposo.MAIN_server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainServer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainServer.class.getResource("/com/drimtim/projectrsacasariposo/server/serverSplash.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Server's Console");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        stage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });
        stage.setAlwaysOnTop(true);
    }

    public static void main(String[] args) {
        launch();
    }
}