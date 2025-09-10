package com.jFastApi.http.interceptor;

import java.util.*;

public class InterceptorRegistry {
    private static final NavigableMap<Integer, Interceptor> interceptors = new TreeMap<>();
    private static final NavigableMap<Integer, Interceptor> systemInterceptors = new TreeMap<>();

    static void register(int order, Interceptor interceptor) {
        interceptors.put(order, interceptor);
    }

    static void registerSystemInterceptor(int order, Interceptor interceptor) {
        systemInterceptors.put(order, interceptor);
    }

    public static Collection<Interceptor> getInterceptors() {
        List<Interceptor> all = new ArrayList<>();
        all.addAll(systemInterceptors.values());
        all.addAll(interceptors.values());
        return all;
    }
}
