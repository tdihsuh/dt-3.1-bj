package com.hansight.kunlun.analysis.utils.v2;

import java.io.*;
import java.util.*;


class OrderlessPair {
    private String first;
    private String second;

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof OrderlessPair))
            return false;

        OrderlessPair other = (OrderlessPair) obj;

        return (first.equals(other.first) &&
                second.equals(other.second));
    }

    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    public OrderlessPair(String first, String second) {
        if (first.compareTo(second) > 0) {
            this.first = second;
            this.second = first;
        } else {
            this.first = first;
            this.second = second;
        }
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
}


/**
 * Created by zhaoss on 14-10-19.
 */
public class EditDistanceUrlCluster {
    private HashMap<OrderlessPair, Integer> dist = new HashMap<OrderlessPair, Integer>();
   /* import com.google.commons.commons
            Long2IntHashMap dist;
    urli_hash = ursl.binarySearch(urls[i]); // Int
    long pair = Int2Long(urli_hash, urlj_hash);
*/
    public static int normalizedEditDistance(String s1, String s2, int upperLimit) {
        double normalizeFactor = 100.0 / Math.max(s1.length(), s2.length());

        int n = editDistance(s1, s2, (int) (upperLimit / normalizeFactor));
        n = (int) (n * normalizeFactor);

        return n;
    }

    /**
     * Levenshtein
     *
     * @param first
     * @param second
     * @param upperLimit
     * @return
     */
    public static int editDistance(String first, String second, int upperLimit) {
        int len1 = first.length();
        int len2 = second.length();

        if (Math.abs(len1 - len2) > upperLimit) {
            return upperLimit + 10; // To avoid unnecessary computing
        }

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                if (first.charAt(i) == second.charAt(j)) {
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    dp[i + 1][j + 1] = min(dp[i][j], dp[i][j + 1], dp[i + 1][j]) + 1;

                }
            }
        }
        return dp[len1][len2];
    }

    private static int min(int first, int second, int three) {
        return Math.min(Math.min(first, second), three);
    }

    protected OPTICS<String> algorithm;

    protected final int eps = 20;
    protected final int Minpts = 3;

    public void cluster(Collection<String> urls) throws Exception {

        algorithm = new OPTICS<String>() {
            public int distance(String p1, String p2, int upperLimit) {
                OrderlessPair pair = new OrderlessPair(p1, p2);
                Integer n = dist.get(pair);
                if (n == null) {
                    n = normalizedEditDistance(pair.getFirst(), pair.getSecond(), upperLimit);
                    dist.put(pair, n);
                } else {
                    dist.remove(pair);
                }

                return n;
            }
        };

      /*  for int i = 0 ; i < urls.length; i++
                for int j = i + 1; j < urls.length; j++
                    n = normalizedEditDistance(urls[i], urls[j], upperLimit);
                    if (n < upperLimit)
                        dist.put(pair, h);*/

        algorithm.cluster(urls, eps, Minpts);
    }

    public boolean isOutlier(String str) {
        ArrayList<OPTICS<String>.Point> result = algorithm.result();

        int count = 0;

        for (int i = 0; i < result.size() && count < Minpts; i++) {
            OPTICS<String>.Point point = result.get(i);

            int dist = normalizedEditDistance(str, point.point, eps);
            if (dist < eps) {
                if (point.reachable != OPTICS.UNDEFINED
                        || (i + 1 < result.size() && result.get(i + 1).reachable != OPTICS.UNDEFINED))
                    count++;
            }
        }

        return count < Minpts;
    }

    public static void main(String[] args) throws Exception {
        EditDistanceUrlCluster cluster = new EditDistanceUrlCluster();

        Set<String> urls200 = new LinkedHashSet<>();
        Set<String> urls404 = new LinkedHashSet<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "iso-8859-1"));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            String[] s = line.trim().split("\\s+");

            String cs_uri_stem = s[1];
            String sc_status = s[2];

            if (sc_status.equals("200")) {
                if (!urls200.contains(cs_uri_stem)) urls200.add(cs_uri_stem);
            } else if (sc_status.equals("404")) {
                if (!urls404.contains(cs_uri_stem)) urls404.add(cs_uri_stem);
            }
        }
        in.close();

        Stopwatch watch = new Stopwatch();
        watch.start();

        cluster.cluster(urls200);

        System.out.println("Done clustering in " + watch.reset() + " ms");

        PrintWriter out = new PrintWriter(new File(args[0] + ".404"), "iso-8859-1");

        for (String url : urls404) {
            out.print(url);

            if (cluster.isOutlier(url))
                out.print(" Malicious");

            out.println();
        }

        out.close();

        System.out.println("Done detecting outliers in " + watch.reset() + " ms");

    }
}
