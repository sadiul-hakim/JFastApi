package com.jFastApi.enumeration;

public enum ContentType {
    JSON("application/json"),
    XML("application/xml"),
    HTML("text/html"),
    TEXT("text/plain"),
    FORM("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data");

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return mimeType;
    }

    // Optional: lookup by string
    public static ContentType fromString(String value) {
        for (ContentType type : values()) {
            if (type.mimeType.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + value);
    }
}

