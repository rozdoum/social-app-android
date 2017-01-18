package com.rozdoum.socialcomponents.enums;

/**
 * Created by alexey on 05.12.16.
 */

public enum UploadImagePrefix {

    PROFILE("profile_"), POST("post_");

    String prefix;

    UploadImagePrefix(String prefix) {
        this.prefix = prefix;
    }

    private String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return getPrefix();
    }
}