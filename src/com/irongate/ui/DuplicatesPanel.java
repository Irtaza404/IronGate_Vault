package com.irongate.ui;

import com.irongate.model.VaultFile;

import com.irongate.service.FileService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DuplicatesPanel {

    private final FileService fileService = new FileService();
    @SuppressWarnings("unused")
	private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @SuppressWarnings({ "unchecked", "exports" })
    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef0f8;");

        // Stat cards
        long[] stats = fileService.getStats();
        HBox statRow = new HBox(12,
            statCard(String.valueOf(stats[0]), "TOTAL FILES"),
            statCard(String.valueOf(stats[1]), "ENCRYPTED"),
            statCard(String.valueOf(stats[2]), "DUPLICATES"),
            statCard(humanSize(stats[3]),      "STORAGE USED")
        );
        for (javafx.scene.Node n : statRow.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        // Table card
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(StyleUtil.CARD_STYLE);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        Label tableTitle = new Label("Duplicate Files");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        Button scanBtn = new Button("🔍  Scan for Duplicates");
        scanBtn.setStyle(StyleUtil.BTN_PRIMARY);

        HBox header = new HBox(tableTitle);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(sp, scanBtn);

        // Columns: FILE NAME, MATCH, TYPE, SIZE, ACTION
        TableView<VaultFile> table = new TableView<>();
        table.setPlaceholder(new Label("No duplicates found yet\nRun a scan to discover duplicate files."));
        table.setStyle("-fx-background-color: transparent;");

        TableColumn<VaultFile, String> nameCol = col("FILE NAME", 220);
        nameCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getFileName()));

        TableColumn<VaultFile, String> matchCol = col("MATCH", 80);
        matchCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty("100%"));

        TableColumn<VaultFile, String> typeCol = col("TYPE", 70);
        typeCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getFileType()));

        TableColumn<VaultFile, String> sizeCol = col("SIZE", 90);
        sizeCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getDisplaySize()));

        TableColumn<VaultFile, Void> actionCol = col("ACTION", 140);
        actionCol.setCellFactory(c -> new TableCell<>() {
            final Button delBtn = new Button("Delete");
            {
                delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444;" +
                                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                VaultFile vf = getTableView().getItems().get(getIndex());
                delBtn.setOnAction(e -> {
                    fileService.delete(vf);
                    getTableView().getItems().remove(vf);
                });
                setGraphic(delBtn);
            }
        });

        table.getColumns().addAll(nameCol, matchCol, typeCol, sizeCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableCard.getChildren().addAll(header, table);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        root.getChildren().addAll(statRow, tableCard);

        // ── Scan action ──────────────────────────────────────────
        scanBtn.setOnAction(e -> {
            List<VaultFile> dups = fileService.getDuplicates();
            table.getItems().setAll(dups);
            if (dups.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.INFORMATION,
                    "No duplicate files found in your vault.");
                a.setTitle("Scan Complete"); a.setHeaderText(null); a.showAndWait();
            }
        });

        return root;
    }

    private <T> TableColumn<VaultFile, T> col(String title, double width) {
        TableColumn<VaultFile, T> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setStyle("-fx-font-size: 11px;");
        return c;
    }

    private VBox statCard(String value, String label) {
        Label valLbl  = new Label(value);
        valLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
        VBox card = new VBox(4, valLbl, nameLbl);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(StyleUtil.CARD_STYLE);
        return card;
    }

    private String humanSize(long bytes) {
        if (bytes < 1024)               return bytes + " B";
        if (bytes < 1024 * 1024)        return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
