package com.jFastApi.http.interceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorRegistry {
    private static final List<Interceptor> interceptors = new ArrayList<>();

    public static void register(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static List<Interceptor> getInterceptors() {
        return interceptors;
    }
}
