package com.irongate.ui;

import com.irongate.service.AuthService;

import com.irongate.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SecurityPanel {

    private final AuthService authService = new AuthService();

    @SuppressWarnings("exports")
	public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef0f8;");

        VBox card = new VBox(20);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.setStyle(StyleUtil.CARD_STYLE);

        Label title = new Label("Security Settings");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        // ── 2FA section ──────────────────────────────────────────
        VBox twoFABox = new VBox(8);
        twoFABox.setPadding(new Insets(16));
        twoFABox.setStyle("-fx-background-color: #f4f6ff; -fx-background-radius: 10;" +
                          "-fx-border-color: #e0e4ff; -fx-border-radius: 10; -fx-border-width: 1;");

        Label twoFATitle = new Label("Two-Factor Authentication (2FA)");
        twoFATitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");

        Label twoFADesc = new Label(
            "When enabled, a 6-digit OTP will be sent to your registered\n" +
            "email address every time you log in. Requires SMTP configuration.");
        twoFADesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        boolean currently2FA = authService.is2FAEnabled();
        Label statusLbl = new Label(currently2FA ? "Status: ENABLED" : "Status: DISABLED");
        statusLbl.setStyle(currently2FA
            ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
            : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        Button toggleBtn = new Button(currently2FA ? "Disable 2FA" : "Enable 2FA");
        toggleBtn.setStyle(currently2FA ? StyleUtil.BTN_OUTLINE : StyleUtil.BTN_PRIMARY);

        toggleBtn.setOnAction(e -> {
            boolean now = authService.is2FAEnabled();
            authService.toggle2FA(!now);
            boolean updated = authService.is2FAEnabled();
            statusLbl.setText(updated ? "Status: ENABLED" : "Status: DISABLED");
            statusLbl.setStyle(updated
                ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            toggleBtn.setText(updated ? "Disable 2FA" : "Enable 2FA");
            toggleBtn.setStyle(updated ? StyleUtil.BTN_OUTLINE : StyleUtil.BTN_PRIMARY);

            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "2FA has been " + (updated ? "enabled." : "disabled."));
            a.setTitle("Security Updated"); a.setHeaderText(null); a.showAndWait();
        });

        twoFABox.getChildren().addAll(twoFATitle, twoFADesc, statusLbl, toggleBtn);

        // ── Encryption info ──────────────────────────────────────
        VBox encBox = new VBox(8);
        encBox.setPadding(new Insets(16));
        encBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10;" +
                        "-fx-border-color: #bbf7d0; -fx-border-radius: 10; -fx-border-width: 1;");

        Label encTitle = new Label("Encryption Status");
        encTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");

        Label encInfo = new Label(
            "✔  AES-256-GCM encryption active\n" +
            "✔  All files stored as ciphertext only\n" +
            "✔  Session key cleared on logout\n" +
            "✔  Passwords stored as PBKDF2-HMAC-SHA256 hashes");
        encInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #15803d;");

        encBox.getChildren().addAll(encTitle, encInfo);

        // ── Account info ─────────────────────────────────────────
        VBox acctBox = new VBox(8);
        acctBox.setPadding(new Insets(16));
        acctBox.setStyle("-fx-background-color: #fffbeb; -fx-background-radius: 10;" +
                         "-fx-border-color: #fde68a; -fx-border-radius: 10; -fx-border-width: 1;");

        Label acctTitle = new Label("Account Information");
        acctTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");

        String username = SessionManager.isLoggedIn()
                          ? SessionManager.getCurrentUser().getUsername() : "N/A";
        String email    = SessionManager.isLoggedIn()
                          ? SessionManager.getCurrentUser().getEmail()    : "N/A";

        Label acctInfo = new Label("Username: " + username + "\nEmail: " + email);
        acctInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #78350f;");

        acctBox.getChildren().addAll(acctTitle, acctInfo);

        card.getChildren().addAll(title, twoFABox, encBox, acctBox);
        root.getChildren().add(card);

        return root;
    }
}
