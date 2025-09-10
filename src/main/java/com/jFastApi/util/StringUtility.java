package com.jFastApi.util;

public final class StringUtility {
    private StringUtility() {
    }

    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }
}
