package com.irongate.ui;

import com.irongate.service.AuthService;

import com.irongate.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DashboardScreen {

    private final AuthService authService = new AuthService();
    private BorderPane root;
    private Button activeBtn;

    @SuppressWarnings("exports")
	public Pane build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #eef0f8;");
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());
        showMyFiles();
        return root;
    }

    // ── Top bar ──────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setMinHeight(58);
        bar.setStyle(
            "-fx-background-color: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.12), 10, 0, 0, 3);"
        );

        // Logo + name
        ImageView logo = logoImage(34);
        Label name = new Label("IRONGATE VAULT");
        name.setStyle(
            "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a1a2e;" +
            "-fx-letter-spacing: 1;"
        );
        Label tagline = new Label("Secure Vault");
        tagline.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaa;");
        VBox nameBox = new VBox(0, name, tagline);
        HBox brand = new HBox(10, logo, nameBox);
        brand.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // User chip
        String username = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getUsername() : "Guest";
        String initials = username.length() >= 2
            ? username.substring(0, 2).toUpperCase()
            : username.substring(0, 1).toUpperCase();

        Circle avatarCircle = new Circle(16);
        avatarCircle.setFill(Color.web("#667eea"));
        Label initLbl = new Label(initials);
        initLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatar = new StackPane(avatarCircle, initLbl);

        Label userLbl = new Label(username.toUpperCase());
        userLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-weight: bold;");

        HBox userChip = new HBox(8, avatar, userLbl);
        userChip.setAlignment(Pos.CENTER);
        userChip.setPadding(new Insets(6, 14, 6, 10));
        userChip.setStyle(
            "-fx-background-color: #f4f6ff; -fx-background-radius: 20;" +
            "-fx-border-color: #e0e4ff; -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Button logoutBtn = new Button("LOGOUT");
        logoutBtn.setStyle(StyleUtil.BTN_OUTLINE);
        logoutBtn.setOnAction(e -> { authService.logout(); MainApp.showLogin(); });

        bar.getChildren().addAll(brand, spacer, userChip, new Label("  "), logoutBtn);
        return bar;
    }

    // ── Sidebar ──────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(3);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setMinWidth(186);
        sidebar.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 2, 0, 0);"
        );

        // Section label
        Label navLabel = navSection("MAIN");

        Button myFiles  = navBtn("🗂", "My Files");
        Button dups     = navBtn("🔍", "Duplicates");
        Button actLog   = navBtn("📋", "Activity Log");

        Label secLabel  = navSection("ACCOUNT");

        Button security = navBtn("🔒", "Security");
        Button profile  = navBtn("👤", "Profile");
        Button cracker  = navBtn("🔑", "Pass Cracker");

        myFiles .setOnAction(e -> { setActive(myFiles);  showMyFiles(); });
        dups    .setOnAction(e -> { setActive(dups);     showDuplicates(); });
        actLog  .setOnAction(e -> { setActive(actLog);   showActivityLog(); });
        security.setOnAction(e -> { setActive(security); showSecurity(); });
        profile .setOnAction(e -> { setActive(profile);  showProfile(); });
        cracker .setOnAction(e -> { setActive(cracker);  showCracker(); });

        // Bottom version label
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Label ver = new Label("v1.0  AES-256-GCM");
        ver.setStyle("-fx-font-size: 9px; -fx-text-fill: #ccc; -fx-padding: 0 0 0 8;");

        sidebar.getChildren().addAll(
            navLabel, myFiles, dups, actLog,
            new Separator(),
            secLabel, security, profile, cracker,
            spacer, ver
        );

        setActive(myFiles);
        return sidebar;
    }

    private Label navSection(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #bbb;" +
            "-fx-padding: 10 0 4 10;"
        );
        return l;
    }

    private Button navBtn(String icon, String text) {
        Button b = new Button(icon + "  " + text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(StyleUtil.SIDEBAR_ITEM_NORMAL);
        b.setOnMouseEntered(e -> {
            if (b != activeBtn) b.setStyle(
                "-fx-background-color: #f0f2ff; -fx-background-radius: 8;" +
                "-fx-text-fill: #667eea; -fx-cursor: hand; -fx-padding: 10 16 10 16;"
            );
        });
        b.setOnMouseExited(e -> {
            if (b != activeBtn) b.setStyle(StyleUtil.SIDEBAR_ITEM_NORMAL);
        });
        return b;
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.setStyle(StyleUtil.SIDEBAR_ITEM_NORMAL);
        btn.setStyle(StyleUtil.SIDEBAR_ITEM_ACTIVE);
        activeBtn = btn;
    }

    // ── Status bar ───────────────────────────────────────────────

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(5, 20, 5, 20));
        bar.setStyle("-fx-background-color: #1a1a2e;");

        Label dot1 = dot("#22c55e");
        Label vault = statusLbl("VAULT ACTIVE");
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        Label dot2 = dot("#667eea");
        Label sess = statusLbl("SESSION: " + (SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getUsername().toUpperCase() : "NONE"));
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

        Label dot3 = dot("#a855f7");
        Label enc = statusLbl("AES-256-GCM ACTIVE");

        bar.getChildren().addAll(dot1, vault, sp1, dot2, sess, sp2, dot3, enc);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private Label dot(String color) {
        Label l = new Label("● ");
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 8px;");
        return l;
    }

    private Label statusLbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 10px; -fx-font-family: monospace;");
        return l;
    }

    // ── Panel switchers ──────────────────────────────────────────

    private void showMyFiles()     { wrap(new MyFilesPanel().build()); }
    private void showDuplicates()  { wrap(new DuplicatesPanel().build()); }
    private void showActivityLog() { wrap(new ActivityLogPanel().build()); }
    private void showSecurity()    { wrap(new SecurityPanel().build()); }
    private void showProfile()     { wrap(new ProfilePanel().build()); }
    private void showCracker()     { wrap(new PasswordCrackerPanel().build()); }

    private void wrap(Region  content) {
        BorderPane wrapper = new BorderPane();
        wrapper.setCenter(content);
        wrapper.setBottom(buildStatusBar());
        root.setCenter(wrapper);
    }

    private ImageView logoImage(double size) {
        try { return new ImageView(new Image("file:logo.png", size, size, true, true)); }
        catch (Exception e) {
            ImageView iv = new ImageView(); iv.setFitWidth(size); iv.setFitHeight(size); return iv;
        }
    }
}
