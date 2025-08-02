package com.example.wirebarley.enumeration;

public enum AccountStatus {
    ACTIVE, // 활성
    INACTIVE, // 비활성
    DORMANT, // 휴먼
    DELETED; // 삭제

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isNotActive() {
        return this != ACTIVE;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
