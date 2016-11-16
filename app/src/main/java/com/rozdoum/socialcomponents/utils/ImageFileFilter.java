package com.rozdoum.socialcomponents.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by alexey on 01.11.16.
 */

public class ImageFileFilter implements FileFilter {
    //TODO: this class are not used. Remove it.
    private final String[] okFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};

    public boolean accept(File file) {
        for (String extension : okFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}