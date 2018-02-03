package com.hansight.kunlun.analysis.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QuartileUtils {
    /**
     * Retrive the quartile value from an array
     * .
     *
     * @param values       THe array of data
     * @param lowerPercent The percent cut off. For the lower quartile use 25,
     *                     for the upper-quartile use 75
     * @return double
     */
    public static double quartile(double[] values, int lowerPercent) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }

        // Rank order the values
      /*  double[] v = new double[values.length];
        System.arraycopy(values, 0, v, 0, values.length);
        Arrays.sort(v);*/

        int n = index(lowerPercent, values.length);
        return values[n];

    }

    public static double quartile(Map<Double, Integer> values, int lowerPercent, int length) {
        if (values == null || length == 0) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        int n = index(lowerPercent, length);
        int index = 0;
        for (Map.Entry<Double, Integer> entry : values.entrySet()) {
            index += entry.getValue();
            if (index >= n) {
                return entry.getKey();
            }
        }
        // Rank order the values
      /*  double[] v = new double[values.length];
        System.arraycopy(values, 0, v, 0, values.length);
        Arrays.sort(v);*/


        return -1;

    }

    private static int index(int lowerPercent, int len) {
        if (lowerPercent == 0) {
            return 0;
        }
        lowerPercent = lowerPercent % 100;
        if (lowerPercent == 0) {
            return len;
        }
        return len * lowerPercent / 100;


    }

    /**
     * Retrive the quartile value from an array
     * .
     *
     * @param values       THe array of data
     * @param lowerPercent The percent cut off. For the lower quartile use 25,
     *                     for the upper-quartile use 75
     * @return int
     */
    public static int quartile(int[] values, int lowerPercent) {

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }

        //TODO why copy？？ Rank order the values
       /* int[] v = new int[values.length];
        System.arraycopy(values, 0, v, 0, values.length);
        Arrays.sort(v);*/
        // Arrays.sort(values);


        /*lowerPercent = lowerPercent % 100;
        int n = values.length * lowerPercent / 100;//Math no`t need*/
        int n = index(lowerPercent, values.length);

        return values[n];

    }

    public static int upperFence(List<Integer> values) {
        int[] v = new int[values.size()];
        for (int i = 0; i < v.length; i++)
            v[i] = values.get(i);
        return upperFence(v);
    }

    public static double upperFenceD(List<Double> values) {
        double[] v = new double[values.size()];
        for (int i = 0; i < v.length; i++)
            v[i] = values.get(i);
        return upperFence(v);
    }

    public static int upperFence(int[] values) {
        Arrays.sort(values);
        int lower = QuartileUtils.quartile(values, 25);
        int upper = QuartileUtils.quartile(values, 75);
        return upper + (int) (1.5 * (upper - lower));
    }

    public static double upperFence(double[] values) {
        Arrays.sort(values);
        double lower = QuartileUtils.quartile(values, 25);
        double upper = QuartileUtils.quartile(values, 75);
        return upper + (int) (1.5 * (upper - lower));
    }

    public static double upperFence(Map<Double, Integer> values, int len) {
        double lower = QuartileUtils.quartile(values, 25, len);
        double upper = QuartileUtils.quartile(values, 75, len);
        return upper + (int) (1.5 * (upper - lower));
    }

    public static int lowerFence(List<Integer> values) {
        int[] v = new int[values.size()];
        for (int i = 0; i < v.length; i++)
            v[i] = values.get(i);
        return lowerFence(v);
    }

    public static int lowerFence(int[] values) {
        Arrays.sort(values);
        int lower = QuartileUtils.quartile(values, 25);
        int upper = QuartileUtils.quartile(values, 75);
        return lower - (int) (1.5 * (upper - lower));
    }
}
