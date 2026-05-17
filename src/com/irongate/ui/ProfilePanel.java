package com.irongate.ui;

import com.irongate.dao.ActivityLogDAO;

import com.irongate.dao.UserDAO;
import com.irongate.model.User;
import com.irongate.security.BCryptUtil;
import com.irongate.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;

public class ProfilePanel {

    private final UserDAO        userDAO = new UserDAO();
    private final ActivityLogDAO logDAO  = new ActivityLogDAO();

    @SuppressWarnings("exports")
	public  Region build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #eef0f8; -fx-background-color: #eef0f8;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #eef0f8;");

        User user = SessionManager.getCurrentUser();
        if (user == null) {
            root.getChildren().add(new Label("Not logged in."));
            scroll.setContent(root);
            return scroll;
        }

        // ── Profile header card ──────────────────────────────────
        VBox profileCard = new VBox(12);
        profileCard.setPadding(new Insets(28, 32, 28, 32));
        profileCard.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #a855f7);" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 16, 0, 0, 4);"
        );
        profileCard.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle with initials
        StackPane avatar = buildAvatar(user.getUsername());

        VBox nameBox = new VBox(4);
        Label nameLbl = new Label(user.getUsername());
        nameLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label emailLbl = new Label(user.getEmail());
        emailLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        Label memberLbl = new Label("Member since " +
            (user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "N/A"));
        memberLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.6);");
        nameBox.getChildren().addAll(nameLbl, emailLbl, memberLbl);

        HBox profileHeader = new HBox(20, avatar, nameBox);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        profileCard.getChildren().add(profileHeader);

        // ── Edit username card ───────────────────────────────────
        VBox usernameCard = editCard(
            "👤  Change Username",
            "Update your display name",
            "#667eea"
        );
        Label userNameLbl = fieldLabel("NEW USERNAME");
        TextField usernameField = styledField("Enter new username");
        usernameField.setText(user.getUsername());
        Label userStatusLbl = new Label(); statusStyle(userStatusLbl);
        Button saveUserBtn = primaryBtn("Save Username");

        saveUserBtn.setOnAction(e -> {
            String newName = usernameField.getText().trim();
            if (newName.isEmpty() || newName.equals(user.getUsername())) {
                setStatus(userStatusLbl, "Enter a different username.", false); return;
            }
            if (newName.length() < 3) {
                setStatus(userStatusLbl, "Username must be at least 3 characters.", false); return;
            }
            if (userDAO.updateUsername(user.getUserId(), newName)) {
                user.setUsername(newName);
                nameLbl.setText(newName);
                logDAO.log(user.getUserId(), null, "PROFILE_USERNAME_CHANGED");
                setStatus(userStatusLbl, "✅ Username updated successfully!", true);
            } else {
                setStatus(userStatusLbl, "❌ Username already taken.", false);
            }
        });
        usernameCard.getChildren().addAll(userNameLbl, usernameField, userStatusLbl, saveUserBtn);

        // ── Edit email card ──────────────────────────────────────
        VBox emailCard = editCard("📧  Change Email", "Update your registered email address", "#22c55e");
        Label emailFldLbl = fieldLabel("NEW EMAIL");
        TextField emailField = styledField("Enter new email");
        emailField.setText(user.getEmail());
        Label emailStatusLbl = new Label(); statusStyle(emailStatusLbl);
        Button saveEmailBtn = primaryBtn("Save Email");
        saveEmailBtn.setStyle(saveEmailBtn.getStyle().replace("#667eea", "#22c55e").replace("#a855f7","#16a34a"));

        saveEmailBtn.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            if (newEmail.isEmpty() || newEmail.equals(user.getEmail())) {
                setStatus(emailStatusLbl, "Enter a different email address.", false); return;
            }
            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                setStatus(emailStatusLbl, "Enter a valid email address.", false); return;
            }
            if (userDAO.updateEmail(user.getUserId(), newEmail)) {
                user.setEmail(newEmail);
                emailLbl.setText(newEmail);
                logDAO.log(user.getUserId(), null, "PROFILE_EMAIL_CHANGED");
                setStatus(emailStatusLbl, "✅ Email updated successfully!", true);
            } else {
                setStatus(emailStatusLbl, "❌ Email already in use by another account.", false);
            }
        });
        emailCard.getChildren().addAll(emailFldLbl, emailField, emailStatusLbl, saveEmailBtn);

        // ── Change password card ─────────────────────────────────
        VBox passCard = editCard("🔑  Change Password", "Choose a strong new password", "#f59e0b");
        Label curPassLbl  = fieldLabel("CURRENT PASSWORD");
        PasswordField curPassField  = styledPwd("Current password");
        Label newPassLbl  = fieldLabel("NEW PASSWORD");
        PasswordField newPassField  = styledPwd("New password (min 8 chars)");
        Label confPassLbl = fieldLabel("CONFIRM NEW PASSWORD");
        PasswordField confPassField = styledPwd("Confirm new password");

        // Password strength bar
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setMaxWidth(Double.MAX_VALUE);
        strengthBar.setStyle("-fx-accent: #ef4444;");
        Label strengthLbl = new Label("Password strength");
        strengthLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        newPassField.textProperty().addListener((obs, old, val) -> {
            int score = scorePassword(val);
            strengthBar.setProgress(score / 5.0);
            String[] labels = {"", "Very Weak", "Weak", "Moderate", "Strong", "Very Strong"};
            String[] colors = {"", "#ef4444", "#f97316", "#eab308", "#22c55e", "#16a34a"};
            if (score > 0) {
                strengthBar.setStyle("-fx-accent: " + colors[score] + ";");
                strengthLbl.setText("Strength: " + labels[score]);
                strengthLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[score] + ";");
            } else {
                strengthLbl.setText("Password strength");
                strengthLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");
            }
        });

        Label passStatusLbl = new Label(); statusStyle(passStatusLbl);
        Button savePassBtn = primaryBtn("Update Password");
        savePassBtn.setStyle(savePassBtn.getStyle().replace("#667eea","#f59e0b").replace("#a855f7","#d97706")
                             .replace("white","#1a1a2e"));

        savePassBtn.setOnAction(e -> {
            String cur  = curPassField.getText();
            String nw   = newPassField.getText();
            String conf = confPassField.getText();

            if (!BCryptUtil.verify(cur, user.getPasswordHash())) {
                setStatus(passStatusLbl, "❌ Current password is incorrect.", false); return;
            }
            if (nw.length() < 8) {
                setStatus(passStatusLbl, "❌ New password must be at least 8 characters.", false); return;
            }
            if (!nw.equals(conf)) {
                setStatus(passStatusLbl, "❌ Passwords do not match.", false); return;
            }
            String newHash = BCryptUtil.hash(nw);
            if (userDAO.updatePassword(user.getUserId(), newHash)) {
                user.setPasswordHash(newHash);
                curPassField.clear(); newPassField.clear(); confPassField.clear();
                logDAO.log(user.getUserId(), null, "PROFILE_PASSWORD_CHANGED");
                setStatus(passStatusLbl, "✅ Password updated successfully!", true);
            } else {
                setStatus(passStatusLbl, "❌ Failed to update password.", false);
            }
        });

        passCard.getChildren().addAll(
            curPassLbl, curPassField,
            newPassLbl, newPassField,
            strengthBar, strengthLbl,
            confPassLbl, confPassField,
            passStatusLbl, savePassBtn
        );

        root.getChildren().addAll(profileCard, usernameCard, emailCard, passCard);
        scroll.setContent(root);
        return scroll;
    }

    // ── Builder helpers ──────────────────────────────────────────

    private StackPane buildAvatar(String username) {
        Circle circle = new Circle(32);
        circle.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ffffff", 0.3)),
            new Stop(1, Color.web("#ffffff", 0.1))));
        circle.setStroke(Color.web("#ffffff", 0.5));
        circle.setStrokeWidth(2);

        String initials = username.length() >= 2
            ? String.valueOf(username.charAt(0)).toUpperCase() +
              String.valueOf(username.charAt(1)).toUpperCase()
            : username.substring(0, 1).toUpperCase();

        Label initLbl = new Label(initials);
        initLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        StackPane sp = new StackPane(circle, initLbl);
        sp.setMinSize(64, 64);
        sp.setMaxSize(64, 64);
        return sp;
    }

    private VBox editCard(String title, String subtitle, String accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle(StyleUtil.CARD_STYLE +
            "-fx-border-left-color: " + accentColor + ";" +
            "-fx-border-left-width: 4;" +
            "-fx-border-width: 0 0 0 4;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-style: solid inside;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #f0f0ff;");

        card.getChildren().addAll(titleLbl, subLbl, sep);
        return card;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888;");
        return l;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(StyleUtil.INPUT_STYLE);
        tf.setMaxWidth(480);
        return tf;
    }

    private PasswordField styledPwd(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(StyleUtil.INPUT_STYLE);
        pf.setMaxWidth(480);
        return pf;
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.setStyle(StyleUtil.BTN_PRIMARY);
        b.setPrefWidth(200);
        return b;
    }

    private void statusStyle(Label l) {
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
        l.setWrapText(true);
    }

    private void setStatus(Label l, String msg, boolean success) {
        l.setText(msg);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (success ? "#22c55e" : "#ef4444") + ";");
    }

    private int scorePassword(String pw) {
        int score = 0;
        if (pw.length() >= 8)               score++;
        if (pw.length() >= 12)              score++;
        if (pw.matches(".*[A-Z].*"))        score++;
        if (pw.matches(".*[0-9].*"))        score++;
        if (pw.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }
}
