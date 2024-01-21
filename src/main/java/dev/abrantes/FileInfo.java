package dev.abrantes;

import java.io.File;

public class FileInfo {

    private File file;
    private String hash;

    private boolean isNotified = false;

    public FileInfo(File file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public File getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public boolean isUserNotified() {
        return isNotified;
    }

    public void setNotified(boolean isNotified) {
        this.isNotified = isNotified;
    }
}
