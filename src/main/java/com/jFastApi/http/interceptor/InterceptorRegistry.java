package com.jFastApi.http.interceptor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InterceptorRegistry {
    private static final List<Interceptor> interceptors = new CopyOnWriteArrayList<>();

    public static void register(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static List<Interceptor> getInterceptors() {
        return interceptors;
    }
}
