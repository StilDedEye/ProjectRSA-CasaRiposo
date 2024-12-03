package com.drimtim.projectrsacasariposo.MAIN_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainClient extends Application {

    public static Stage primaryStage;
    public static Scene currentScene;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainClient.class.getResource("/com/drimtim/projectrsacasariposo/client/clientSplash.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        currentScene = scene;
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        stage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });
        stage.setAlwaysOnTop(true);
        primaryStage = stage;

        stage.getIcons().add(new Image(Objects.requireNonNull(MainClient.class.getResourceAsStream("/com/drimtim/projectrsacasariposo/client/icon/iconRadioClient.png"))));
    }

    public static void main(String[] args) {
        launch();
    }

}