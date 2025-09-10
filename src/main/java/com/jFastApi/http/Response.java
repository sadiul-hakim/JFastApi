package com.jFastApi.http;

import com.jFastApi.enumeration.ContentType;
import com.jFastApi.enumeration.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Response<T> {

    private final T body;
    private final HttpStatus status;
    private final ContentType contentType;
    private final long contentLength;
    private final Map<String, String> headers;
    private final boolean keepAlive;

    private Response(Builder<T> builder) {
        this.body = builder.body;
        this.status = builder.status;
        this.contentType = builder.contentType;
        this.contentLength = builder.contentLength;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.keepAlive = builder.keepAlive;
    }

    public T getBody() {
        return body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    // --- Builder ---
    public static class Builder<T> {
        private T body;
        private HttpStatus status = HttpStatus.OK;
        private ContentType contentType = ContentType.JSON;
        private long contentLength;
        private Map<String, String> headers = new HashMap<>();
        private boolean keepAlive = false;

        public Builder<T> body(T body) {
            this.body = body;
            if (body instanceof String s) {
                this.contentLength = s.getBytes(StandardCharsets.UTF_8).length;
            } else if (body instanceof byte[] b) {
                this.contentLength = b.length;
            }
            return this;
        }

        public Builder<T> status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder<T> contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder<T> header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder<T> headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder<T> keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Response<T> build() {
            if (status == null) throw new IllegalStateException("HttpStatus must be set");
            if (contentType == null) throw new IllegalStateException("ContentType must be set");
            return new Response<>(this);
        }
    }
}

