package com.example.CineBook.common.constant;

import lombok.Getter;

@Getter
public enum ProductCategory {
    COMBO("Combo"),
    DRINK("Đồ uống"),
    SNACK("Đồ ăn vặt"),
    POPCORN("Bỏng ngô");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
}
