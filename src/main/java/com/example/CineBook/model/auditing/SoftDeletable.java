package com.example.CineBook.model.auditing;

public interface SoftDeletable {
    Boolean getIsDelete();
    void setDeleted(Boolean deleted);
}
