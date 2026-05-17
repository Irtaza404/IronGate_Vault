package com.irongate.ui;

import com.irongate.model.VaultFile;

import com.irongate.service.FileService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyFilesPanel {

    private final FileService fileService = new FileService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private TableView<VaultFile> table;

    @SuppressWarnings("exports")
	public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef0f8;");

        // ── Stat cards ───────────────────────────────────────────
        long[] stats = fileService.getStats();
        HBox statRow = new HBox(12,
            statCard(String.valueOf(stats[0]), "TOTAL FILES",   "📁", "#667eea"),
            statCard(String.valueOf(stats[1]), "ENCRYPTED",     "🔒", "#22c55e"),
            statCard(String.valueOf(stats[2]), "DUPLICATES",    "⚠", "#f59e0b"),
            statCard(humanSize(stats[3]),      "STORAGE USED",  "💾", "#a855f7")
        );
        for (javafx.scene.Node n : statRow.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        // ── Table card ───────────────────────────────────────────
        VBox tableCard = new VBox(14);
        tableCard.setPadding(new Insets(22));
        tableCard.setStyle(StyleUtil.CARD_STYLE);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        // Header
        Label tableTitle = new Label("My Files");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1a1a2e;");
        Label subtitle = new Label("All files stored with AES-256-GCM encryption");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
        VBox titleBox = new VBox(2, tableTitle, subtitle);

        Button uploadBtn = new Button("⬆  Upload File");
        uploadBtn.setStyle(StyleUtil.BTN_PRIMARY);
        uploadBtn.setPrefHeight(38);

        HBox header = new HBox(titleBox);
        header.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(sp, uploadBtn);

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        tableCard.getChildren().addAll(header, new Separator(), table);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        root.getChildren().addAll(statRow, tableCard);
        refreshTable();

        // ── Upload ───────────────────────────────────────────────
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select File to Upload");
            File file = chooser.showOpenDialog(uploadBtn.getScene().getWindow());
            if (file == null) return;

            FileService.UploadOutcome outcome = fileService.upload(file);
            switch (outcome.result()) {
                case SUCCESS -> {
                    showInfo("✅ Uploaded", "\"" + file.getName() + "\" encrypted and stored.");
                    refreshTable();
                }
                case DUPLICATE -> {
                    // Custom styled duplicate dialog
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setTitle("Duplicate Detected");
                    a.setHeaderText("This file already exists in your vault.");
                    a.setContentText("File: " + file.getName() + "\nDo you still want to keep it?");
                    ButtonType keep   = new ButtonType("Keep Anyway", ButtonBar.ButtonData.YES);
                    ButtonType discard = new ButtonType("Discard",   ButtonBar.ButtonData.CANCEL_CLOSE);
                    a.getButtonTypes().setAll(keep, discard);
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == keep) {
                            FileService.UploadOutcome forced = fileService.forceUpload(file);
                            if (forced.result() == FileService.UploadResult.SUCCESS) {
                                showInfo("✅ Kept", "Duplicate file saved as a copy.");
                                refreshTable();
                            } else {
                                showError("Error", "Could not save the duplicate copy.");
                            }
                        }
                        // else discard — do nothing
                    });
                }
                case ERROR -> showError("Upload Failed", "Could not encrypt or store the file.");
            }
        });

        return root;
    }

    @SuppressWarnings("unchecked")
    private TableView<VaultFile> buildTable() {
        TableView<VaultFile> tbl = new TableView<>();
        tbl.setPlaceholder(buildPlaceholder());
        tbl.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: #f0f2ff;");
        tbl.setRowFactory(rv -> {
            TableRow<VaultFile> row = new TableRow<>();
            row.setStyle("-fx-background-color: white;");
            row.hoverProperty().addListener((obs, wasHovered, isHovered) ->
                row.setStyle(isHovered && !row.isEmpty()
                    ? "-fx-background-color: #f8f9ff;"
                    : "-fx-background-color: white;"));
            return row;
        });

        TableColumn<VaultFile, String> nameCol = col("FILE NAME", 220);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        nameCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                VaultFile vf = getTableRow().getItem();
                String icon = vf != null ? fileIcon(vf.getFileType()) : "📄";
                Label lbl = new Label(icon + "  " + item);
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
                setGraphic(lbl);
            }
        });

        TableColumn<VaultFile, String> typeCol = col("TYPE", 65);
        typeCol.setCellValueFactory(new PropertyValueFactory<>("fileType"));
        typeCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item.toUpperCase());
                badge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #7c3aed;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-padding: 2 6 2 6;");
                setGraphic(badge);
            }
        });

        TableColumn<VaultFile, String> sizeCol = col("SIZE", 85);
        sizeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDisplaySize()));

        TableColumn<VaultFile, String> dateCol = col("UPLOADED", 140);
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getUploadDate() != null ? d.getValue().getUploadDate().format(FMT) : ""));

        TableColumn<VaultFile, String> statusCol = col("STATUS", 80);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("fileStatus"));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle("active".equals(item)
                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;" +
                      "-fx-font-size: 10px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 10; -fx-padding: 2 8 2 8;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;" +
                      "-fx-font-size: 10px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 10; -fx-padding: 2 8 2 8;");
                setGraphic(badge);
            }
        });

        TableColumn<VaultFile, Void> actionCol = col("ACTIONS", 280);
        actionCol.setCellFactory(c -> new TableCell<>() {
            final Button viewBtn  = actionBtn("👁 View",     "#e0f2fe", "#0284c7");
            final Button editBtn  = actionBtn("✏ Edit",     "#fef9c3", "#ca8a04");
            final Button dlBtn    = actionBtn("⬇ Download", "#ede9fe", "#7c3aed");
            final Button delBtn   = actionBtn("🗑 Delete",  "#fee2e2", "#dc2626");

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                VaultFile vf = getTableView().getItems().get(getIndex());
                HBox box = new HBox(5, viewBtn, editBtn, dlBtn, delBtn);
                box.setAlignment(Pos.CENTER_LEFT);

                viewBtn.setOnAction(e -> openViewDialog(vf));

                editBtn.setOnAction(e -> openEditDialog(vf));

                dlBtn.setOnAction(e -> {
                    FileChooser chooser = new FileChooser();
                    chooser.setInitialFileName(vf.getFileName());
                    File dest = chooser.showSaveDialog(getScene().getWindow());
                    if (dest != null) {
                        if (fileService.downloadTo(vf, dest))
                            showInfo("Downloaded", "Saved to " + dest.getName());
                        else showError("Failed", "Decryption error.");
                    }
                });

                delBtn.setOnAction(e -> {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Confirm Delete"); a.setHeaderText(null);
                    a.setContentText("Delete \"" + vf.getFileName() + "\"?\nThis action cannot be undone.");
                    a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            fileService.delete(vf);
                            getTableView().getItems().remove(vf);
                        }
                    });
                });

                setGraphic(box);
            }
        });

        tbl.getColumns().addAll(nameCol, typeCol, sizeCol, dateCol, statusCol, actionCol);
        return tbl;
    }

    // ── View dialog (decrypt → show in window) ───────────────────

    private void openViewDialog(VaultFile vf) {
        byte[] data = fileService.decryptToMemory(vf);
        if (data == null) { showError("Error", "Decryption failed."); return; }

        Stage dlg = new Stage();
        dlg.setTitle("Viewing: " + vf.getFileName());
        dlg.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1a1a2e;");

        // Header
        Label title = new Label("👁  " + vf.getFileName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: white;");

        HBox meta = new HBox(16,
            metaChip("Type",    vf.getFileType()),
            metaChip("Size",    vf.getDisplaySize()),
            metaChip("Status",  vf.getFileStatus())
        );

        // Determine content type
        String type = vf.getFileType() == null ? "" : vf.getFileType().toLowerCase();
        boolean isImage = type.matches("png|jpg|jpeg|gif|bmp|webp");
        boolean isText  = type.matches("txt|csv|log|xml|json|java|py|js|html|css|sql|md|bat|sh");

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background: #0d0d1a; -fx-background-color: #0d0d1a;");
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);

        if (isImage) {
            ImageView iv = new ImageView(new Image(new java.io.ByteArrayInputStream(data)));
            iv.setPreserveRatio(true);
            iv.setFitWidth(560);
            StackPane imgPane = new StackPane(iv);
            imgPane.setStyle("-fx-background-color: #0d0d1a; -fx-padding: 10;");
            scroll.setContent(imgPane);
        } else if (isText) {
            TextArea ta = new TextArea(new String(data, java.nio.charset.StandardCharsets.UTF_8));
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setStyle("-fx-control-inner-background: #0d0d1a; -fx-text-fill: #00ff88;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-border-color: transparent;");
            ta.setPrefHeight(420);
            scroll.setContent(ta);
        } else {
            // Binary — show hex preview
            StringBuilder hex = new StringBuilder();
            int previewBytes = Math.min(data.length, 512);
            for (int i = 0; i < previewBytes; i++) {
                if (i % 16 == 0) hex.append(String.format("\n%04X:  ", i));
                hex.append(String.format("%02X ", data[i]));
            }
            if (data.length > 512) hex.append("\n... [binary file — ").append(data.length).append(" bytes total]");
            TextArea ta = new TextArea(hex.toString().trim());
            ta.setEditable(false);
            ta.setStyle("-fx-control-inner-background: #0d0d1a; -fx-text-fill: #667eea;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            ta.setPrefHeight(420);
            scroll.setContent(ta);
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(StyleUtil.BTN_OUTLINE);
        closeBtn.setOnAction(e -> dlg.close());
        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(title, meta, scroll, footer);
        dlg.setScene(new Scene(layout, 620, 540));
        dlg.show();
    }

    // ── Edit dialog (rename) ─────────────────────────────────────

    private void openEditDialog(VaultFile vf) {
        Stage dlg = new Stage();
        dlg.setTitle("Edit File");
        dlg.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(28));
        layout.setMaxWidth(400);
        layout.setStyle("-fx-background-color: white;");

        Label title = new Label("✏  Edit File Details");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        Label nameLbl = new Label("FILE NAME");
        nameLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888;");
        TextField nameField = new TextField(vf.getFileName());
        nameField.setStyle(StyleUtil.INPUT_STYLE);

        Label noteLbl = new Label("Note: Only the display name changes. The encrypted file on disk is not modified.");
        noteLbl.setWrapText(true);
        noteLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa; -fx-wrap-text: true;");

        Button saveBtn   = new Button("Save Changes");
        Button cancelBtn = new Button("Cancel");
        saveBtn.setStyle(StyleUtil.BTN_PRIMARY);
        cancelBtn.setStyle(StyleUtil.BTN_OUTLINE);

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");

        layout.getChildren().addAll(title, nameLbl, nameField, noteLbl, errorLbl, btnRow);

        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) { errorLbl.setText("Name cannot be empty."); return; }
            boolean ok = fileService.renameFile(vf, newName);
            if (ok) { refreshTable(); dlg.close(); }
            else errorLbl.setText("Failed to rename. Try again.");
        });
        cancelBtn.setOnAction(e -> dlg.close());

        dlg.setScene(new Scene(layout, 420, 280));
        dlg.show();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void refreshTable() {
        List<VaultFile> files = fileService.getMyFiles();
        table.getItems().setAll(files);
    }

    private VBox buildPlaceholder() {
        Label icon = new Label("📭");
        icon.setStyle("-fx-font-size: 40px;");
        Label msg  = new Label("No files in vault");
        msg.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label sub  = new Label("Upload a file to get started");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
        VBox box = new VBox(6, icon, msg, sub);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Button actionBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                   "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-size: 11px;" +
                   "-fx-font-weight: bold; -fx-padding: 4 8 4 8;");
        return b;
    }

    private Label metaChip(String key, String val) {
        Label l = new Label(key + ": " + val);
        l.setStyle("-fx-background-color: #2a2a50; -fx-text-fill: #a0a0d0;" +
                   "-fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
        return l;
    }

    private String fileIcon(String ext) {
        if (ext == null) return "📄";
        return switch (ext.toLowerCase()) {
            case "pdf"           -> "📕";
            case "png","jpg","jpeg","gif","bmp","webp" -> "🖼";
            case "mp4","avi","mov","mkv" -> "🎬";
            case "mp3","wav","aac" -> "🎵";
            case "zip","rar","7z" -> "📦";
            case "docx","doc"    -> "📘";
            case "xlsx","xls"    -> "📗";
            case "pptx","ppt"    -> "📙";
            case "java","py","js","sql" -> "💻";
            case "txt","log","md" -> "📝";
            default              -> "📄";
        };
    }

    private <T> TableColumn<VaultFile, T> col(String title, double width) {
        TableColumn<VaultFile, T> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        c.setReorderable(false);
        return c;
    }

    private VBox statCard(String value, String label, String icon, String color) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        Label valLbl  = new Label(value);
        valLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #999; -fx-font-weight: bold;");
        VBox card = new VBox(4, iconLbl, valLbl, nameLbl);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(StyleUtil.CARD_STYLE + "-fx-border-color: transparent; -fx-border-width: 0;" +
            "-fx-border-radius: 12;");
        return card;
    }

    private String humanSize(long bytes) {
        if (bytes < 1024)             return bytes + " B";
        if (bytes < 1024 * 1024)      return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L*1024*1024)  return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
