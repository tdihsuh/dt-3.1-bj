package com.hansight.kunlun.analysis.statistics.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldPattern {
    public final boolean rough_;
    public final String field_;
    protected final Pattern pattern_;

    protected FieldPattern(boolean rough, String field) {
        rough_ = rough;
        field_ = field;
        pattern_ = null;
    }

    protected FieldPattern(boolean rough, String field, String pattern) {
        rough_ = rough;
        field_ = field;
        pattern_ = Pattern.compile(pattern);
    }

    public String match(String value) {
        Matcher matcher = pattern_.matcher(value);
        if (matcher.find()) {
            return (matcher.groupCount() >= 1)? matcher.group(1) : matcher.group(0);
        } else
            return null;
    }

    public static FieldPattern Parse(boolean rough, String fieldPattern) {
        int pos = fieldPattern.indexOf('/');

        if (pos < 0) {
            return new FieldPattern(rough, fieldPattern);
        } else {
            return new FieldPattern(rough, fieldPattern.substring(0, pos), fieldPattern.substring(pos + 1));
        }
    }

}