package com.irongate.ui;

import com.irongate.service.AuthService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class LoginScreen {

    private final AuthService authService = new AuthService();
    private Label errorLabel;

    @SuppressWarnings("exports")
	public Pane build() {
        // Root — dark background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + ";");

        // Card
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(380);
        card.setStyle(StyleUtil.CARD_STYLE);

        // Logo
        ImageView logo = logoImage(72);

        // Title
        Text title = new Text("IronGate  Vault");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.web("#1a1a2e"));

        Text subtitle = new Text("secure.vault.login");
        subtitle.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        subtitle.setFill(Color.web("#999"));

        VBox logoBox = new VBox(6, logo, title, subtitle);
        logoBox.setAlignment(Pos.CENTER);

        // Username
        Label userLbl = fieldLabel("USERNAME");
        TextField userField = styledField("Username");

        // Password
        Label passLbl = fieldLabel("PASSWORD");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle(StyleUtil.INPUT_STYLE);
        passField.setMaxWidth(Double.MAX_VALUE);

        // Forgot password
        Hyperlink forgot = new Hyperlink("Forgot password?");
        forgot.setStyle("-fx-font-size: 11px; -fx-text-fill: #667eea; -fx-border-color: transparent;");
        HBox forgotBox = new HBox(forgot);
        forgotBox.setAlignment(Pos.CENTER_RIGHT);

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setStyle(StyleUtil.BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        // Register link
        Label regPrompt = new Label("Don't have an account?");
        regPrompt.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
        Button regBtn = new Button("Register Me");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea;" +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");

        HBox regBox = new HBox(4, regPrompt, regBtn);
        regBox.setAlignment(Pos.CENTER);

        // Separator
        Separator sep = new Separator();

        // Version footer
        Label version = new Label("System version 10.4  Security Protocol");
        version.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        card.getChildren().addAll(
            logoBox,
            userLbl, userField,
            passLbl, passField,
            forgotBox,
            errorLabel,
            loginBtn,
            regBox,
            sep,
            version
        );

        root.getChildren().add(card);

        // ── Actions ─────────────────────────────────────────────
        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("Please enter username and password.");
                return;
            }
            errorLabel.setText("Connecting…");
            AuthService.LoginResult result = authService.login(u, p);
            switch (result) {
                case SUCCESS         -> MainApp.showDashboard();
                case NEED_OTP        -> MainApp.showOTP(u);
                case BAD_CREDENTIALS -> errorLabel.setText("Invalid username or password.");
                case ERROR           -> errorLabel.setText("Server error — check SMTP config.");
            }
        });

        passField.setOnAction(e -> loginBtn.fire());

        regBtn.setOnAction(e -> MainApp.showRegister());

        return root;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(StyleUtil.INPUT_STYLE);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888;");
        return l;
    }

    private ImageView logoImage(double size) {
        try {
            Image img = new Image("file:logo.png", size, size, true, true);
            return new ImageView(img);
        } catch (Exception e) {
            ImageView iv = new ImageView();
            iv.setFitWidth(size); iv.setFitHeight(size);
            return iv;
        }
    }
}
