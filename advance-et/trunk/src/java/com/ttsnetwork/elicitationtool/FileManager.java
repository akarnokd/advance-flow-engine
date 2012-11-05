package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author TTS
 */
public class FileManager {

    private Map<String,FileItem> files = new HashMap<String,FileItem>();
    private static FileManager instance = new FileManager();

    public static FileManager getInstance() {
        return instance;
    }

    public Collection<FileItem> getFiles() {
        if (files.isEmpty()) {
            for (File f : FileViewData.getXmlFileRepository().listFiles()) {
                if (f.isFile()) {
                    files.put(f.getName(), new FileItem(f));
                }
            }
        }
        return files.values();
    }

    public FileItem getItem(String filename) {
        return files.get(filename);
    }

    void add(File f) {
        files.put(f.getName(), new FileItem(f));
    }

    void remove(File f) {
        files.remove(f.getName());
    }


}
