package com.hansight.kunlun.analysis.utils;

/**
 * Author:zhhui
 * DateTime:2014/7/3 17:05.
 */
public class IPFilter {
    public static boolean isInnerIP(String ip) {
        String[] phases = ip.split("\\.");
        if (phases.length != 4)
            throw new IllegalArgumentException("error IP phase! must be 0.0.0.0~255.255.255.255");
        if ("10".equals(phases[0]))
            return true;
        if ("192".equals(phases[0]) && "168".equals(phases[1]))
            return true;
        if ("172".equals(phases[0])) {
            int phase = Integer.valueOf(phases[1]);
            if (phase >= 16 && phase <= 31)
                return true;
        }
        return false;
    }
}
