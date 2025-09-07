package com.jFastApi.util;

public final class BannerUtility {
    private BannerUtility() {
    }

    private static final String BANNER_FILE_NAME = "banner.txt";
    private static String BANNER;

    static {

        var is = BannerUtility.class.getClassLoader().getResourceAsStream(BANNER_FILE_NAME);
        BANNER = FileUtility.readStream(is);
    }

    public static String getBANNER() {
        return BANNER;
    }
}
