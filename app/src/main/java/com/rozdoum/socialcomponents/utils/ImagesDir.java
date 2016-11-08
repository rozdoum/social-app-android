package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Kristina on 7/23/14.
 */
public class ImagesDir {

    private static final String TEMP_IMAGES_PATH = "images/temp";
    private static File imagesTempDir;

    public static File getTempImagesDir(Context context) {
        if (imagesTempDir == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                imagesTempDir = context.getExternalFilesDir(TEMP_IMAGES_PATH);
            } else {
                imagesTempDir = context.getCacheDir();
            }
        }

        if (imagesTempDir != null && !imagesTempDir.exists()) {
            imagesTempDir.mkdirs();
        }

        return imagesTempDir;
    }

    public static boolean isTempImagesDir(String path, Context context) {
        try {
            return path.startsWith(getTempImagesDir(context).getCanonicalPath());
        } catch (IOException e) {
            LogUtil.logError("isTempImagesDir", "Failed to get temp images folder", e);
        }

        return false;
    }

    public static void cleanDirs(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                cleanDirs(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();

    }

}
