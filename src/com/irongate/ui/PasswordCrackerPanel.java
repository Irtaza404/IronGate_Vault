package com.irongate.ui;

import com.irongate.service.PasswordCrackerService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PasswordCrackerPanel {

    private final PasswordCrackerService crackerService = new PasswordCrackerService();

    @SuppressWarnings("exports")
	public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef0f8;");

        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setMaxWidth(600);
        card.setStyle(StyleUtil.CARD_STYLE);

        Label title = new Label("Password Cracker");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        // Password input
        Label passLbl = new Label("Enter Password");
        passLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: bold;");
        TextField passField = new TextField();
        passField.setPromptText("Enter a password to analyze");
        passField.setStyle(StyleUtil.INPUT_STYLE);
        passField.setMaxWidth(Double.MAX_VALUE);

        // Method select
        Label methodLbl = new Label("Select Method");
        methodLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: bold;");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll(
            "Quick strength check",
            "Dictionary attack simulation",
            "Brute-force simulation",
            "Full analysis (all methods)"
        );
        methodBox.setValue("Quick strength check");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        methodBox.setStyle(StyleUtil.INPUT_STYLE);

        // Run button
        Button runBtn = new Button("▶  Run Analysis");
        runBtn.setStyle(StyleUtil.BTN_PRIMARY);

        // Progress bar
        ProgressBar progress = new ProgressBar(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setStyle("-fx-accent: linear-gradient(to right, #667eea, #f093fb);");
        progress.setVisible(false);

        // Result panel
        VBox resultBox = new VBox(8);
        resultBox.setPadding(new Insets(14));
        resultBox.setStyle("-fx-background-color: #f4f6ff; -fx-background-radius: 10;");
        resultBox.setVisible(false);

        Label attemptsLbl  = new Label();
        Label timeLbl      = new Label();
        Label strengthLbl  = new Label();
        Label strategyLbl  = new Label();

        for (Label l : new Label[]{attemptsLbl, timeLbl, strengthLbl, strategyLbl})
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        resultBox.getChildren().addAll(attemptsLbl, timeLbl, strengthLbl, strategyLbl);

        // Note
        Label note = new Label(
            "Note: This section is for simulation and security assessment only. " +
            "Do not use it for unauthorized password cracking.");
        note.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-wrap-text: true;" +
                      "-fx-background-color: #fff8e1; -fx-background-radius: 8;" +
                      "-fx-padding: 10 14 10 14;");
        note.setWrapText(true);

        card.getChildren().addAll(
            title,
            passLbl, passField,
            methodLbl, methodBox,
            runBtn,
            progress,
            resultBox,
            note
        );

        root.getChildren().add(card);

        // ── Run action ───────────────────────────────────────────
        runBtn.setOnAction(e -> {
            String pw = passField.getText();
            if (pw.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please enter a password to analyze.");
                a.setHeaderText(null); a.showAndWait(); return;
            }

            runBtn.setDisable(true);
            progress.setVisible(true);
            progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            resultBox.setVisible(false);

            Thread thread = new Thread(() -> {
                PasswordCrackerService.CrackResult result =
                    crackerService.analyse(pw, attempts ->
                        Platform.runLater(() ->
                            progress.setProgress(Math.min(attempts / 100_000.0, 1.0))
                        )
                    );

                Platform.runLater(() -> {
                    progress.setProgress(1.0);
                    resultBox.setVisible(true);
                    runBtn.setDisable(false);

                    attemptsLbl.setText("Simulated attempts: " +
                        String.format("%,d", result.attempts()));

                    String timeStr;
                    long ms = result.estimatedMs();
                    if (ms < 1000)           timeStr = ms + " ms";
                    else if (ms < 60_000)    timeStr = (ms / 1000) + " seconds";
                    else if (ms < 3_600_000) timeStr = (ms / 60_000) + " minutes";
                    else                     timeStr = (ms / 3_600_000) + "+ hours";

                    timeLbl.setText("Estimated real-world crack time: " + timeStr);

                    String strength = result.strength();
                    String color = switch (strength) {
                        case "Very Weak"   -> "#ef4444";
                        case "Weak"        -> "#f97316";
                        case "Moderate"    -> "#eab308";
                        case "Strong"      -> "#22c55e";
                        case "Very Strong" -> "#16a34a";
                        default            -> "#333";
                    };
                    strengthLbl.setText("Password strength: " + strength);
                    strengthLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

                    strategyLbl.setText(result.message());
                });
            });
            thread.setDaemon(true);
            thread.start();
        });

        return root;
    }
}
