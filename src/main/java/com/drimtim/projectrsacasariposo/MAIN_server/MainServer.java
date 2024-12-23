package com.drimtim.projectrsacasariposo.MAIN_server;

import com.drimtim.projectrsacasariposo.MAIN_client.MainClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainServer extends Application {

    public static Stage primaryStage;
    public static Scene currentScene;

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
        primaryStage = stage;
        currentScene = scene;

        stage.getIcons().add(new Image(Objects.requireNonNull(MainClient.class.getResourceAsStream("/com/drimtim/projectrsacasariposo/server/icon/iconRadioServer.png"))));
    }

    public static void main(String[] args) {
        launch();
    }
}