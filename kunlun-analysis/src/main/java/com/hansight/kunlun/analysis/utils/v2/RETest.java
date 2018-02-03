package com.hansight.kunlun.analysis.utils.v2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoss on 14-10-8.
 */
public class RETest {
    public static String match(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return (matcher.groupCount() >= 1)? matcher.group(1) : matcher.group(0);
        } else
            return null;
    }

    public static void main(String[] args) throws Exception {
        String str1 = "218.88.92.172-1676667152_20a550f74ec58741f711386677682612 218.88.92.172-1676667152";

        Pattern pattern1 = Pattern.compile("([0-9.]*-[0-9]*)");
//        Pattern pattern2 = Pattern.compile("([0-9a-f]+)");
       System.out.println(match(pattern1, str1));
//        System.out.println(match(pattern2, str1));


    }
}