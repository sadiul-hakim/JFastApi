package com.jFastApi.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public final class JsonUtility {
    private JsonUtility() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T fromJson(InputStream input, Class<T> clazz) throws IOException {
        return mapper.readValue(input, clazz);
    }

    public static String toJson(Object obj) {

        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception ex) {
            return "";
        }
    }
}
