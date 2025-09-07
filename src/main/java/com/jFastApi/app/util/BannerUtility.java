package com.jFastApi.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BannerUtility {
    private BannerUtility() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BannerUtility.class);

    private static final String BANNER_FILE_NAME = "banner.txt";
    private static String BANNER;

    static {

        try {

            var is = BannerUtility.class.getClassLoader().getResourceAsStream(BANNER_FILE_NAME);
            BANNER = FileUtility.readStream(is);
        } catch (Exception ex) {
            LOGGER.error("Failed to read banner, error {}", ex.getMessage());
        }
    }

    public static String getBANNER() {
        return BANNER;
    }
}
