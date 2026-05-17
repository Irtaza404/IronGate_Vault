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

public class OTPScreen {

    private final String      username;
    private final AuthService authService = new AuthService();
    private Label errorLabel;

    public OTPScreen(String username) { this.username = username; }

    @SuppressWarnings("exports")
	public Pane build() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + ";");

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(360);
        card.setStyle(StyleUtil.CARD_STYLE);

        // Logo
        ImageView logo = logoImage(80);
        Text title = new Text("IronGate  Vault");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.web("#1a1a2e"));
        Text sub = new Text("enter-main.2FA");
        sub.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
        sub.setFill(Color.web("#999"));
        VBox logoBox = new VBox(5, logo, title, sub);
        logoBox.setAlignment(Pos.CENTER);

        // Instruction
        Label instr = new Label("Enter the 6-digit code sent to your email.");
        instr.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-wrap-text: true;");
        instr.setWrapText(true);
        Label instr2 = new Label("Check your inbox and enter here.");
        instr2.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        // ── 6 individual digit boxes ─────────────────────────────
        TextField[] digits = new TextField[6];
        HBox digitRow = new HBox(8);
        digitRow.setAlignment(Pos.CENTER);

        String digitStyle =
            "-fx-background-color: #f4f6ff;" +
            "-fx-border-color: #c7d0ff;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-border-width: 2;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1a1a2e;" +
            "-fx-alignment: center;" +
            "-fx-pref-width: 48;" +
            "-fx-pref-height: 56;" +
            "-fx-max-width: 48;";

        String digitFocusStyle =
            "-fx-background-color: #eef0ff;" +
            "-fx-border-color: #667eea;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-border-width: 2;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #667eea;" +
            "-fx-alignment: center;" +
            "-fx-pref-width: 48;" +
            "-fx-pref-height: 56;" +
            "-fx-max-width: 48;";

        for (int i = 0; i < 6; i++) {
            digits[i] = new TextField();
            digits[i].setStyle(digitStyle);
            digits[i].setMaxWidth(48);
            final int idx = i;
            final String ds = digitStyle, fs = digitFocusStyle;

            digits[i].focusedProperty().addListener((obs, wasFocused, isFocused) ->
                digits[idx].setStyle(isFocused ? fs : ds));

            digits[i].textProperty().addListener((obs, oldVal, newVal) -> {
                // Only allow single digit
                if (!newVal.matches("\\d*")) {
                    digits[idx].setText(oldVal);
                    return;
                }
                if (newVal.length() > 1) {
                    // If pasted 6 digits, distribute
                    if (newVal.length() == 6) {
                        for (int j = 0; j < 6; j++)
                            digits[j].setText(String.valueOf(newVal.charAt(j)));
                        digits[5].requestFocus();
                        return;
                    }
                    digits[idx].setText(newVal.substring(newVal.length() - 1));
                }
                // Auto-advance to next box
                if (digits[idx].getText().length() == 1 && idx < 5)
                    digits[idx + 1].requestFocus();
            });

            // Backspace moves to previous box
            digits[i].setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.BACK_SPACE) {
                    if (digits[idx].getText().isEmpty() && idx > 0) {
                        digits[idx - 1].clear();
                        digits[idx - 1].requestFocus();
                    }
                }
            });

            digitRow.getChildren().add(digits[i]);
        }

        // Error
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");

        // Verify button
        Button verifyBtn = new Button("Verify OTP");
        verifyBtn.setStyle(StyleUtil.BTN_PRIMARY);
        verifyBtn.setMaxWidth(Double.MAX_VALUE);

        // Resend link
        Hyperlink resend = new Hyperlink("Didn't receive the code? Resend OTP");
        resend.setStyle("-fx-font-size: 11px; -fx-text-fill: #667eea; -fx-border-color: transparent;");

        Hyperlink back = new Hyperlink("Back to Login");
        back.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-border-color: transparent;");

        VBox links = new VBox(4, resend, back);
        links.setAlignment(Pos.CENTER);

        Label version = new Label("System version 10.4  Security Protocol");
        version.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        card.getChildren().addAll(logoBox, instr, instr2, digitRow, errorLabel,
                                   verifyBtn, links, new Separator(), version);

        root.getChildren().add(card);

        // ── Actions ──────────────────────────────────────────────
        verifyBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            for (TextField d : digits) sb.append(d.getText());
            String otp = sb.toString();
            if (otp.length() != 6) { errorLabel.setText("Enter all 6 digits."); return; }

            AuthService.OTPResult res = authService.verifyOTP(username, otp);
            switch (res) {
                case SUCCESS -> MainApp.showDashboard();
                case EXPIRED -> errorLabel.setText("OTP expired. Please log in again.");
                case INVALID -> {
                    errorLabel.setText("Incorrect OTP. Try again.");
                    for (TextField d : digits) { d.clear(); d.setStyle(digitStyle); }
                    digits[0].requestFocus();
                }
            }
        });

        // Enter on last digit fires verify
        digits[5].setOnAction(e -> verifyBtn.fire());
        back.setOnAction(e -> MainApp.showLogin());
        resend.setOnAction(e -> {
            errorLabel.setText("Please log in again to get a new OTP.");
            MainApp.showLogin();
        });

        return root;
    }

    private ImageView logoImage(double size) {
        try { return new ImageView(new Image("file:logo.png", size, size, true, true)); }
        catch (Exception e) { ImageView iv = new ImageView(); iv.setFitWidth(size); iv.setFitHeight(size); return iv; }
    }
}
