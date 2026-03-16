package org.example.ordinara.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

public class FileItem {

    private final Path sourcePath;
    private final StringProperty fileName;
    private final StringProperty extension;
    private final long sizeBytes;
    private final ObjectProperty<FileCategory> category;

    public FileItem(Path sourcePath, String fileName, String extension,
                    long sizeBytes, FileCategory category) {
        this.sourcePath = sourcePath;
        this.fileName = new SimpleStringProperty(fileName);
        this.extension = new SimpleStringProperty(extension);
        this.sizeBytes = sizeBytes;
        this.category = new SimpleObjectProperty<>(category);
    }

    public Path getSourcePath()          { return sourcePath; }
    public String getFileName()          { return fileName.get(); }
    public StringProperty fileNameProperty() { return fileName; }
    public String getExtension()         { return extension.get(); }
    public StringProperty extensionProperty() { return extension; }
    public long getSizeBytes()           { return sizeBytes; }
    public FileCategory getCategory()    { return category.get(); }
    public void setCategory(FileCategory c) { category.set(c); }
    public ObjectProperty<FileCategory> categoryProperty() { return category; }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
