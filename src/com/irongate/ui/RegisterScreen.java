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

public class RegisterScreen {

    private final AuthService authService = new AuthService();
    private Label errorLabel;

    @SuppressWarnings("exports")
	public Pane build() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + ";");

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 48, 36, 48));
        card.setMaxWidth(390);
        card.setStyle(StyleUtil.CARD_STYLE);

        // Logo
        ImageView logo = logoImage(64);
        Text title    = new Text("IronGate  Vault");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.web("#1a1a2e"));
        Text sub = new Text("CREATE ACCOUNT");
        sub.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
        sub.setFill(Color.web("#999"));

        VBox logoBox = new VBox(5, logo, title, sub);
        logoBox.setAlignment(Pos.CENTER);

        // Fields
        Label userLbl   = fieldLabel("USERNAME");
        TextField userF = styledField("Choose a username");

        Label emailLbl   = fieldLabel("EMAIL");
        TextField emailF = styledField("Enter your email");

        Label passLbl      = fieldLabel("PASSWORD");
        PasswordField passF = styledPwd("Create a password");

        Label confLbl      = fieldLabel("CONFIRM PASSWORD");
        PasswordField confF = styledPwd("Confirm your password");

        // Error
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        // Register button
        Button regBtn = new Button("Register Me");
        regBtn.setStyle(StyleUtil.BTN_PRIMARY);
        regBtn.setMaxWidth(Double.MAX_VALUE);

        // Login link
        Label loginPrompt = new Label("Already have an account?");
        loginPrompt.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
        Button loginBtn = new Button("Login here");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea;" +
                          "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");
        HBox loginBox = new HBox(4, loginPrompt, loginBtn);
        loginBox.setAlignment(Pos.CENTER);

        Label version = new Label("System version 10.4  Security Protocol");
        version.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        card.getChildren().addAll(
            logoBox,
            userLbl, userF,
            emailLbl, emailF,
            passLbl, passF,
            confLbl, confF,
            errorLabel,
            regBtn,
            loginBox,
            new Separator(),
            version
        );

        root.getChildren().add(card);

        // ── Actions ──────────────────────────────────────────────
        regBtn.setOnAction(e -> {
            String u  = userF.getText().trim();
            String em = emailF.getText().trim();
            String p  = passF.getText();
            String cp = confF.getText();

            if (u.isEmpty() || em.isEmpty() || p.isEmpty()) {
                errorLabel.setText("All fields are required."); return;
            }
            if (!p.equals(cp)) {
                errorLabel.setText("Passwords do not match."); return;
            }
            if (p.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters."); return;
            }

            errorLabel.setText("Registering…");
            AuthService.RegisterResult res = authService.register(u, em, p);
            switch (res) {
                case SUCCESS       -> { showAlert(Alert.AlertType.INFORMATION, "Registered!",
                                          "Account created. Please log in."); MainApp.showLogin(); }
                case USERNAME_TAKEN -> errorLabel.setText("Username already taken.");
                case EMAIL_TAKEN    -> errorLabel.setText("Email already registered.");
                case ERROR          -> errorLabel.setText("Database error. Try again.");
            }
        });

        loginBtn.setOnAction(e -> MainApp.showLogin());

        return root;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg); a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        tf.setStyle(StyleUtil.INPUT_STYLE); tf.setMaxWidth(Double.MAX_VALUE); return tf;
    }

    private PasswordField styledPwd(String prompt) {
        PasswordField pf = new PasswordField(); pf.setPromptText(prompt);
        pf.setStyle(StyleUtil.INPUT_STYLE); pf.setMaxWidth(Double.MAX_VALUE); return pf;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888;"); return l;
    }

    private ImageView logoImage(double size) {
        try { return new ImageView(new Image("file:logo.png", size, size, true, true)); }
        catch (Exception e) { ImageView iv = new ImageView(); iv.setFitWidth(size); iv.setFitHeight(size); return iv; }
    }
}
