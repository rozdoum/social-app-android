package com.rozdoum.socialcomponents.enums;

/**
 * Created by Alex on 21.07.16.
 */

public enum ItemType {LOAD(10), ITEM(11);
    private final int typeCode;

    ItemType(int typeCode) {
        this.typeCode = typeCode;
    }

    public int getTypeCode() {
        return this.typeCode;
    }
}