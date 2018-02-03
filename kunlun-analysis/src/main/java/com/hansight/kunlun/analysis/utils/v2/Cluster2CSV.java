package com.hansight.kunlun.analysis.utils.v2;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by zhaoss on 14-10-9.
 */
public class Cluster2CSV {
    static class Cluster {
        public int id;

        public Cluster(int id_) {
            id = id_;
        }

        public ArrayList<Integer> samples_ = new ArrayList<Integer>();

    } // class Cluster

    public static void main(String[] args) throws Exception {
        /*
            CSV format:
            size, no_cluster, cluster1, cluster2, cluster3
              1 ,    90     ,         ,         ,
              2 ,   80,     ,      10 ,        8,       7
         */

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "iso-8859-1"));

        ArrayList<Cluster> clusters = new ArrayList<Cluster>();

        int nextID = 0;
        Cluster clusterOrphan = new Cluster(nextID++);

        clusters.add(clusterOrphan);

        int lastSample = -1;
        boolean lastIsUndefined = true;

        try {
            Cluster cluster = null;

            ArrayList<Integer> rows = new ArrayList<Integer>();

            // UNDEFINED, UNDEFINED, 20
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] fields = line.split(" ");

                int sample = Integer.parseInt(fields[1]);

                if (rows.indexOf(sample) < 0)
                    rows.add(sample);

                if (!fields[0].equals("UNDEFINED")) {
                    if (lastIsUndefined) {
                        cluster = new Cluster(nextID++);
                        cluster.samples_.add(lastSample);

                        clusters.add(cluster);
                    }
                    lastIsUndefined = false;
                    cluster.samples_.add(sample);
                } else {
                    // [UNDEFINED], UNDEFINED, 20
                    if (lastIsUndefined && lastSample != -1)
                        clusterOrphan.samples_.add(lastSample);

                    cluster = null;

                    lastIsUndefined = true;
                    lastSample = sample;
                }
            }

            // Now print
            PrintWriter out = new PrintWriter(new File("cluster.csv"), "iso-8859-1");

            // First rows
            out.print("size");
            for (Cluster c : clusters) {
                out.print('\t');
                out.print("C");
                out.print(c.id);
            }
            out.println();

            for (Integer v : rows) {
                out.print(v);

                for (Cluster c : clusters) {
                    out.print("\t");

                    int total = 0;
                    for (Integer s : c.samples_) {
                        if (s == v) total++;
                    }

                    out.print(total);
                }
                out.println();
            }

            out.close();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }

    }

}
