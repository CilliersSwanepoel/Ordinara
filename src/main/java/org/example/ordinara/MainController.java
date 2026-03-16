package org.example.ordinara;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.example.ordinara.model.FileCategory;
import org.example.ordinara.model.FileItem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class MainController implements Initializable {

    @FXML private TextField folderPathField;
    @FXML private TableView<FileItem> fileTable;
    @FXML private TableColumn<FileItem, String> nameColumn;
    @FXML private TableColumn<FileItem, String> extColumn;
    @FXML private TableColumn<FileItem, String> sizeColumn;
    @FXML private TableColumn<FileItem, String> categoryColumn;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button organizeBtn;

    private final ObservableList<FileItem> fileItems = FXCollections.observableArrayList();
    private Path selectedFolder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileTable.setItems(fileItems);

        nameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        extColumn.setCellValueFactory(cell -> cell.getValue().extensionProperty());
        sizeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(FileItem.formatSize(cell.getValue().getSizeBytes())));
        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCategory().getDisplayName()));
    }

    @FXML
    private void onBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Organize");
        File dir = chooser.showDialog(folderPathField.getScene().getWindow());
        if (dir != null) {
            selectedFolder = dir.toPath();
            folderPathField.setText(selectedFolder.toString());
            scanFiles();
        }
    }

    private void scanFiles() {
        fileItems.clear();

        try (Stream<Path> files = Files.list(selectedFolder)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot > 0 && dot < name.length() - 1) {
                    ext = name.substring(dot + 1);
                }

                long size;
                try { size = Files.size(path); } catch (IOException e) { size = 0; }

                fileItems.add(new FileItem(path, name, ext, size, FileCategory.fromExtension(ext)));
            });

            int count = fileItems.size();
            statusLabel.setText(count + (count == 1 ? " file" : " files") + " found");
            organizeBtn.setDisable(count == 0);
        } catch (IOException e) {
            statusLabel.setText("Error scanning folder: " + e.getMessage());
        }
    }

    @FXML
    private void onOrganize() {
        if (selectedFolder == null || fileItems.isEmpty()) return;

        List<FileItem> snapshot = new ArrayList<>(fileItems);
        organizeBtn.setDisable(true);
        progressBar.setVisible(true);

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int total = snapshot.size();
                int moved = 0;

                for (int i = 0; i < total; i++) {
                    FileItem item = snapshot.get(i);
                    Path categoryDir = selectedFolder.resolve(item.getCategory().getDisplayName());
                    Files.createDirectories(categoryDir);

                    Path target = categoryDir.resolve(item.getFileName());
                    target = resolveNameCollision(target, item);
                    Files.move(item.getSourcePath(), target);
                    moved++;

                    updateProgress(i + 1, total);
                    updateMessage(moved + " / " + total + " files organized");
                }

                return moved;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            unbindUI();
            int moved = task.getValue();
            statusLabel.setText("Done! " + moved + " files organized.");
            progressBar.setProgress(1);
            scanFiles();
        });

        task.setOnFailed(e -> {
            unbindUI();
            Throwable ex = task.getException();
            statusLabel.setText("Error: " + (ex != null ? ex.getMessage() : "unknown"));
            progressBar.setVisible(false);
            organizeBtn.setDisable(false);
        });

        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
    }

    private Path resolveNameCollision(Path target, FileItem item) {
        if (!Files.exists(target)) return target;

        String ext = item.getExtension();
        String base = item.getFileName();
        String stem = ext.isEmpty() ? base : base.substring(0, base.length() - ext.length() - 1);

        int counter = 1;
        do {
            String newName = ext.isEmpty()
                    ? stem + " (" + counter + ")"
                    : stem + " (" + counter + ")." + ext;
            target = target.getParent().resolve(newName);
            counter++;
        } while (Files.exists(target));

        return target;
    }

    private void unbindUI() {
        statusLabel.textProperty().unbind();
        progressBar.progressProperty().unbind();
    }
}
