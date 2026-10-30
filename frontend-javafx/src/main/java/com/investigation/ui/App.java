package com.investigation.ui;

import com.investigation.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();

        Scene scene = new Scene(mainController.getRootLayout(), 1280, 820);

        // Load Futuristic Cyber-Forensic Dark Theme
        URL cssResource = getClass().getResource("/css/dark-theme.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        primaryStage.setTitle("Digital Investigation Platform with AI");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
