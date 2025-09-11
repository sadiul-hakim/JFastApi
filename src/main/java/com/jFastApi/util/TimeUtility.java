package com.jFastApi.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class TimeUtility {

    private TimeUtility() {
    }

    /**
     * Converts route time and time unit to a Duration
     */
    public static Duration toDuration(long time, TimeUnit unit) {
        return Duration.of(time, unit.toChronoUnit());
    }
}
