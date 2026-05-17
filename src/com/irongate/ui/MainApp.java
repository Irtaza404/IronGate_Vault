package com.irongate.ui;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.irongate.util.DBConnection;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) {
        primaryStage = stage;
        stage.setTitle("IronGate Vault — Secure File Management");
        stage.setMinWidth(900);
        stage.setMinHeight(620);

        // Try to load the logo as stage icon
        try {
            stage.getIcons().add(new Image("file:logo.png"));
        } catch (Exception ignored) {}

        showLogin();
        stage.show();
    }

    public static void showLogin() {
        Scene scene = new Scene(new LoginScreen().build(), 900, 620);
        primaryStage.setScene(scene);
    }

    public static void showRegister() {
        Scene scene = new Scene(new RegisterScreen().build(), 900, 620);
        primaryStage.setScene(scene);
    }

    public static void showOTP(String username) {
        Scene scene = new Scene(new OTPScreen(username).build(), 900, 620);
        primaryStage.setScene(scene);
    }

    public static void showDashboard() {
        Scene scene = new Scene(new DashboardScreen().build(), 1100, 680);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(680);
    }

    @Override
    public void stop() {
        DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
