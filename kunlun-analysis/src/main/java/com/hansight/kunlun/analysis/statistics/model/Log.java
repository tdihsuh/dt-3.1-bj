package com.hansight.kunlun.analysis.statistics.model;

import java.util.Arrays;

/**
 * Created by zhaoss on 14-10-4.
 */
public class Log {
    protected static final String[] STRING_ARRAY = new String[0];

    public final String roughIDs_;
    public String preciseIDs_;

    public final String[] fields_;
    public final String[] values_;

    public Log(String roughIDs, String preciseIDs, String[] fields, String[] values) {
        roughIDs_ = roughIDs;
        preciseIDs_ = preciseIDs;

        fields_ = fields;
        values_ = values;
    }

    public String getRoughIDs() {
        return roughIDs_;
    }

    public boolean hasPreciseIDs() {
        return preciseIDs_ != null && !preciseIDs_.isEmpty();
    }

    public void setConstructedPreciseIDs(String preciseIDs) {
        preciseIDs_ = preciseIDs;
    }

    public String getPreciseIDs() {
        return preciseIDs_;
    }

    public String getFieldValue(String field) {
        return GetFieldValue(field, fields_, values_);
    }

    public static String GetFieldValue(String field, String[] fields, String[] values) {
        for (int i = 0; i < fields.length; i++) {
            if (Equals(field, fields[i]))
                return values[i];
        }
        return null;
    }

    public static boolean Equals(String str1, String str2) {
        if (str1 == null || str2 == null)
            return str1 == null && str2 == null;
        return str1.equals(str2);
    }

    public static boolean Equals(String[] strs1, String[] strs2) {
        return Arrays.equals(strs1, strs2);
    }

}