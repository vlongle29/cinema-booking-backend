package com.example.CineBook.common.constant;

public enum MovieFormat {
    TWO_D("2D"),
    THREE_D("3D"),
    IMAX("IMAX"),
    IMAX_3D("IMAX 3D"),
    FOUR_DX("4DX"),
    SCREEN_X("ScreenX");

    private final String displayName;

    MovieFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
