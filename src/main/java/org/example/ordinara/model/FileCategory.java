package org.example.ordinara.model;

import java.util.Map;
import java.util.HashMap;

public enum FileCategory {

    IMAGES("Images",
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico", "tiff", "tif"),
    DOCUMENTS("Documents",
            "pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv"),
    VIDEOS("Videos",
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm"),
    MUSIC("Music",
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a"),
    ARCHIVES("Archives",
            "zip", "rar", "7z", "tar", "gz", "bz2"),
    CODE("Code",
            "java", "py", "js", "ts", "html", "css", "cpp", "c", "h", "cs", "rb",
            "go", "rs", "php", "sql", "xml", "json", "yaml", "yml"),
    EXECUTABLES("Executables",
            "exe", "msi", "bat", "cmd", "sh", "jar"),
    OTHER("Other");

    private static final Map<String, FileCategory> LOOKUP = new HashMap<>();

    static {
        for (FileCategory cat : values()) {
            for (String ext : cat.extensions) {
                LOOKUP.put(ext, cat);
            }
        }
    }

    private final String displayName;
    private final String[] extensions;

    FileCategory(String displayName, String... extensions) {
        this.displayName = displayName;
        this.extensions = extensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FileCategory fromExtension(String ext) {
        return LOOKUP.getOrDefault(ext.toLowerCase(), OTHER);
    }
}
