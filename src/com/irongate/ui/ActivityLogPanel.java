package com.irongate.ui;

import com.irongate.dao.ActivityLogDAO;

import com.irongate.model.ActivityLog;
import com.irongate.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivityLogPanel {

    private final ActivityLogDAO logDAO = new ActivityLogDAO();
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("hh:mm a");

    @SuppressWarnings({ "unchecked", "exports" })
    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef0f8;");

        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(StyleUtil.CARD_STYLE);
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Activity Log");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        // Table
        TableView<ActivityLog> table = new TableView<>();
        table.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ActivityLog, String> timeCol = col("TIME", 100);
        timeCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTimestamp() != null
                ? d.getValue().getTimestamp().format(FMT) : ""));

        TableColumn<ActivityLog, String> eventCol = col("EVENT", 220);
        eventCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                friendlyAction(d.getValue().getAction())));

        TableColumn<ActivityLog, String> userCol = col("USER", 120);
        userCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                SessionManager.isLoggedIn()
                ? SessionManager.getCurrentUser().getUsername() : ""));

        TableColumn<ActivityLog, Void> statusCol = col("STATUS", 100);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ActivityLog al = getTableView().getItems().get(getIndex());
                Label badge = new Label(al.getStatus());
                badge.setStyle(StyleUtil.statusStyle(al.getStatus()));
                setGraphic(badge);
            }
        });

        table.getColumns().addAll(timeCol, eventCol, userCol, statusCol);

        // Load data
        if (SessionManager.isLoggedIn()) {
            List<ActivityLog> logs = logDAO.getForUser(
                SessionManager.getCurrentUser().getUserId());
            table.getItems().setAll(logs);
        }

        card.getChildren().addAll(title, table);
        root.getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        return root;
    }

    private String friendlyAction(String action) {
        return switch (action) {
            case "LOGIN"        -> "Logged in";
            case "LOGIN_FAIL"   -> "Login failed";
            case "LOGOUT"       -> "Logged out";
            case "REGISTER"     -> "Account created";
            case "UPLOAD"       -> "File uploaded";
            case "VIEW"         -> "File viewed";
            case "DOWNLOAD"     -> "File downloaded";
            case "DELETE"       -> "File deleted";
            case "OTP_SENT"     -> "OTP sent";
            case "2FA_ENABLED"  -> "2FA enabled";
            case "2FA_DISABLED" -> "2FA disabled";
            default             -> action;
        };
    }

    private <T> TableColumn<ActivityLog, T> col(String title, double width) {
        TableColumn<ActivityLog, T> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setStyle("-fx-font-size: 11px;");
        return c;
    }
}
