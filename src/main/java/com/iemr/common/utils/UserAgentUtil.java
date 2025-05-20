package com.iemr.common.utils;

public class UserAgentUtil {
    public static boolean isMobileDevice(String userAgent) {
        if (userAgent == null) return false;
        String lowerUA = userAgent.toLowerCase();
        return lowerUA.contains("okhttp");
    }
}
