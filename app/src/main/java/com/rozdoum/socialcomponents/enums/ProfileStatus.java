package com.rozdoum.socialcomponents.enums;

/**
 * Created by alexey on 05.12.16.
 */

public enum ProfileStatus {
    PROFILE_CREATED(0), NOT_AUTHORIZED(1), NO_PROFILE(2);

    int status;

    ProfileStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
