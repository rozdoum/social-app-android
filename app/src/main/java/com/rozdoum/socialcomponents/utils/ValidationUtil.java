package com.rozdoum.socialcomponents.utils;

/**
 * Created by Kristina on 8/8/15.
 */
public class ValidationUtil {
    private static final String [] IMAGE_TYPE = new String[]{"jpg", "png", "jpeg", "bmp", "jp2", "psd", "tif", "gif"};

    public static boolean isEmailValid(String email) {
        String stricterFilterString = "[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}";
        return email.matches(stricterFilterString);
    }

    public static boolean isOnlyLatinLetters(String text) {
        String regular = "[a-zA-Z]+";
        return text.matches(regular);
    }

    public static boolean isMonthValid(String monthString) {
        if (monthString != null) {
            if (monthString.length() != 2) {
                return false;
            }

            int month = Integer.valueOf(monthString);
            if (month >= 1 && month <= 12) {
                return true;
            }
        }
        return false;
    }

    public static boolean isYearValid(String monthString) {
        if (monthString != null) {
            if (monthString.length() == 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public static boolean isImage(String path){

        String filenameArray[] = path.split("\\.");
        String extension = filenameArray[filenameArray.length-1];

        if (extension != null) {
            for (String type : IMAGE_TYPE) {
                if (type.toLowerCase().equals(extension.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasExtension(String path){
        String extension = null;
        String filenameArray[] = path.split("\\.");

        if (filenameArray.length > 1) {
            extension = filenameArray[filenameArray.length - 1];
        }

        if (extension != null) {
            return true;
        }
        return false;
    }

    public static boolean containsInvalidSymbol(String name){
        return name.contains("@");
    }

}
