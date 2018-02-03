package com.hansight.kunlun.analysis.utils.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by zhaoss on 14-10-6.
 */
public abstract class OPTICS<T> {
    public static final int UNDEFINED = Integer.MAX_VALUE;

    public  class Point {
        public final T point;
        public int reachable = UNDEFINED;
        private boolean processed = false;

        public Point(T t) {
            point = t;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point1 = (Point) o;

            return !(point != null ? !point.equals(point1.point) : point1.point != null);

        }

        @Override
        public int hashCode() {
            return point != null ? point.hashCode() : 0;
        }
    }

    abstract public int distance(T p1, T p2, int upperLimit);

    /**
     * Return the list of samples of the which the distance is at most eps from sample p
     */
    public ArrayList<Point> neighbors(final ArrayList<Point> data, final Point point, final int eps) {
        ArrayList<Point> all = new ArrayList<Point>(data.size());
        for (Point point1 : data) {
            if (point1 != point && distance(point1.point, point.point, eps) < eps) {
                all.add(point1);
            }
        }

        return all;
    }

    public int core_distance(ArrayList<Point> neighbors, Point point, int eps, int minPts) {
        if (neighbors.size() < minPts)
            return UNDEFINED;

        int dists[] = new int[neighbors.size()];
        for (int i = 0; i < neighbors.size(); i++) {
            dists[i] = distance(neighbors.get(i).point, point.point, eps);
        }

        Arrays.sort(dists);
        return dists[minPts - 1];
    }

    public void update(ArrayList<Point> neighbors, Point point, ArrayList<Point> seeds, int eps, int minPts) {
        int dist = core_distance(neighbors, point, eps, minPts);
        for (Point o : neighbors) {
            if (!o.processed) {
                int new_reach_dist = Math.max(dist, distance(point.point, o.point, eps));
                if (o.reachable == UNDEFINED) {
                    // New sample which is reachable from sample p
                    o.reachable = new_reach_dist;
                    seeds.add(o);
                } else if (new_reach_dist < o.reachable) {
                    // Already processed sample, now update its reachable distance
                    o.reachable = new_reach_dist;
                }

            }
        }

    }

    protected ArrayList<Point> orderedList = null;

    public ArrayList<Point> result() {
        return orderedList;
    }

    public void cluster(T[] db, int eps, int minPts) {
        final ArrayList<Point> DB = new ArrayList<Point>(db.length);
        for (T t : db)
            DB.add(new Point(t));

        cluster(DB, eps, minPts);
    }

    public void cluster(Collection<T> db, int eps, int minPts) {
        final ArrayList<Point> DB = new ArrayList<Point>(db.size());
        for (T t : db)
            DB.add(new Point(t));

        cluster(DB, eps, minPts);
    }

    public void cluster(ArrayList<Point> data, int eps, int minPts) {
        orderedList = new ArrayList<Point>(data.size());

        int totalProcessed;
        do {
            totalProcessed = 0;

            for (Point point : data) {
                if (point.processed) continue;

                ArrayList<Point> neighbor = neighbors(data, point, eps);

                point.processed = true;
                orderedList.add(point);

                if (core_distance(neighbor, point, eps, minPts) != UNDEFINED) {
                    ArrayList<Point> Seeds = new ArrayList<Point>(minPts);
                    update(neighbor, point, Seeds, eps, minPts);

                    for (int i = 0; i < Seeds.size(); i++) {
                        Point q = Seeds.get(i);
                        ArrayList<Point> N1 = neighbors(data, q, eps);

                        q.processed = true;
                        orderedList.add(q);

                        if (core_distance(N1, q, eps, minPts) != UNDEFINED) {
                            update(N1, q, Seeds, eps, minPts);
                        }
                    }
                }

            }
        } while (totalProcessed > 0);
    }




    public static void main(String[] args) {
        OPTICS<Integer> algorithm = new OPTICS<Integer>() {
            public int distance(Integer p1, Integer p2, int upperLimit) {
                return Math.abs(p1 - p2);
            }
        };

        Integer[] DB = {1, 2, 3, 11, 12, 13, 16, 17, 31, 41};
        final int eps = 5;
        final int minPts = 2;

        algorithm.cluster(DB, eps, minPts);

        for (OPTICS.Point point : algorithm.result()) {
            System.out.println(point.point + " " + ((point.reachable == UNDEFINED) ? "UNDEFINED" : point.reachable));
        }

    }
}
