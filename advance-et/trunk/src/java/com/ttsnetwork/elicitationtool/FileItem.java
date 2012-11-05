package com.ttsnetwork.elicitationtool;

import java.io.File;

/**
 *
 * @author TTS
 */
public class FileItem {
    private File file;
    private String user;

    public FileItem(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFilename() {
        return file.getName();
    }

}
