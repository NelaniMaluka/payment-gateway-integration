package com.nelani.demo.model;

public enum PaymentSortField {

    AMOUNT("amount"),
    STATUS("status"),
    PROVIDER("provider"),
    CREATED_AT("createdAt"),
    EXPIRES_AT("expiresAt"),
    COMPLETED_AT("completedAt");

    private final String fieldName;

    PaymentSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
