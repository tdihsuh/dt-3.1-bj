package com.hansight.kunlun.analysis.utils.v2;


import com.hansight.kunlun.analysis.statistics.model.Log;
import com.hansight.kunlun.analysis.statistics.model.SessionSplitter;

import java.io.*;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.*;


class Session {
    public String id;
    public ArrayList<Long> urlHashes = new ArrayList<Long>();

    public Session(String id_) {
        id = id_;
    }
}


/**
 * Data structure to minimize memory requirement of storing a URL session
 **/
class SessionClosed implements Comparable<SessionClosed> {
    public final int length;
    public final long sequenceHash;

    public final ArrayList<Long> urlHashes;

    public final ArrayList<String> ids;

    public static final int UNDEFINED = Integer.MAX_VALUE;
    public int reachability_distance = UNDEFINED;
    public boolean processed = false;


    public SessionClosed(SessionKey key, Session session) {
        length = key.length;
        sequenceHash = key.sequenceHash;

        urlHashes = session.urlHashes;

        ids = new ArrayList<String>();
        ids.add(session.id);
    }

    public void addDuplicates(Session session) {
        ids.add(session.id);
    }

    @Override
    public int compareTo(SessionClosed o) {
        return length - o.length;
    }

}


class SessionKey {
    public final int length;
    public final long sequenceHash;

    public SessionKey(int length_, long sequenceHash_) {
        length = length_;
        sequenceHash = sequenceHash_;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionKey))
            return false;

        SessionKey k = (SessionKey)o;
        return length == k.length && sequenceHash == k.sequenceHash;
    }

    @Override
    public int hashCode() {
        return (int)sequenceHash;
    }
}

class HashUtil {
    // for hashing
    public static final int hashbits = 64;
    public static final BigInteger m = new BigInteger("1000003");
    public static final BigInteger mask = new BigInteger("2").pow(hashbits).subtract(new BigInteger("1"));

    public static long hash(Collection<Long> hashes) {
        if (hashes == null || hashes.size() == 0)
            return 0;

        Iterator<Long> pos = hashes.iterator();

        BigInteger x = BigInteger.valueOf(pos.next());

        while (pos.hasNext()) {
            x = x.multiply(m).xor(BigInteger.valueOf(pos.next())).and(mask);
        }
        x = x.xor(new BigInteger(String.valueOf(hashes.size())));
        if (x.equals(new BigInteger("-1"))) {
            x = new BigInteger("-2");
        }

        return x.longValue();
    }

    public static long hash(String source) {
        if (source == null || source.length() == 0) {
            return 0;
        } else {
            try {
                byte[] sourceArray = source.getBytes("utf-8");
                BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
                BigInteger m = new BigInteger("1000003");
                BigInteger mask = new BigInteger("2").pow(hashbits).subtract(new BigInteger("1"));
                for (byte item : sourceArray) {
                    BigInteger temp = BigInteger.valueOf((long) item);
                    x = x.multiply(m).xor(temp).and(mask);
                }
                x = x.xor(new BigInteger(String.valueOf(source.length())));
                if (x.equals(new BigInteger("-1"))) {
                    x = new BigInteger("-2");
                }
                return x.longValue();
            } catch (UnsupportedEncodingException ex) {
                return 0;
            }
        }
    }

}

class Stopwatch {
    long start = 0, end = 0;

    public void start() {
        start = System.currentTimeMillis();
    }

    public long stop() {
        end = System.currentTimeMillis();

        return (end - start);
    }

    public long reset() {
        end = System.currentTimeMillis();

        long l = (end - start);

        start = end;

        return l;
    }

}

public class SessionCluster extends SessionSplitter {
    class Tmp {
        public HashMap<String, Session> sessions = new HashMap<String, Session>();
        public HashMap<SessionKey, SessionClosed> lookup = new HashMap<SessionKey, SessionClosed>();
        public ArrayList<SessionClosed> sessionsClosed = new ArrayList<SessionClosed>();
    }
    Tmp tmp = new Tmp();

    public SessionClosed[] sessionsClosed;

    public void closeSession(Session session) {
        // Calculate sequence hash
        SessionKey key = new SessionKey(session.urlHashes.size(), HashUtil.hash(session.urlHashes));
        SessionClosed duplicates = tmp.lookup.get(key);
        if (duplicates != null) {
            duplicates.addDuplicates(session);
            return;
        }

        SessionClosed sessionClosed = new SessionClosed(key, session);
        tmp.lookup.put(key, sessionClosed);
        tmp.sessionsClosed.add(sessionClosed);
    }

    public void closeAllSessions() {
        for (Iterator<Session> pos = tmp.sessions.values().iterator(); pos.hasNext(); ) {
            Session session = pos.next();
            closeSession(session);
        }

        sessionsClosed = new SessionClosed[tmp.sessionsClosed.size()];
        tmp.sessionsClosed.toArray(sessionsClosed);

        tmp = null;

        Arrays.sort(sessionsClosed);
    }

    static class Neighbor {
        public final int dist;
        public final SessionClosed session;

        public Neighbor(int dist_, SessionClosed session_) {
            dist = dist_;
            session = session_;
        }
    }

    public ArrayList<Neighbor> getNeighbors(final SessionClosed session, final int eps) {
        ArrayList<Neighbor> all = new ArrayList<Neighbor>();

//        int eps = (int)(session.length * epsFactor);
//        if (eps == 0)
//            return all;

        final int[] bound = jaccardDistanceBound(session.length, eps);
        bound[0] = Math.max(0, bound[0]);
        bound[1] = Math.min(bound[1], sessionsClosed.length - 1);

        int pos = Arrays.binarySearch(sessionsClosed, session, new Comparator<SessionClosed>() {
            @Override
            public int compare(SessionClosed o1, SessionClosed key) {
                return o1.length - bound[0];
            }
        });

        if (pos < 0) pos = -pos - 1;

        for (int i = pos; i >=0; i--) {
            SessionClosed s2 = sessionsClosed[i];
            if (s2 == session) continue;

            if (s2.length < bound[0])
                break;

            int dist = jaccardDistance(session.urlHashes, s2.urlHashes);
            if (dist < eps)
                all.add(new Neighbor(dist, s2));
        }

        for (int i = pos + 1; i < sessionsClosed.length; i++) {
            SessionClosed s2 = sessionsClosed[i];
            if (s2 == session) continue;

            if (s2.length > bound[1])
                break;

            int dist = jaccardDistance(session.urlHashes, s2.urlHashes);
            if (dist < eps)
                all.add(new Neighbor(dist, s2));
        }

        return all;
    }

//    public static boolean distanceLessThan(SessionClosed s1, SessionClosed s2, int eps) {
//        if (Math.abs(s1.length - s2.length) > eps)
//            return false;
//
//        return editDistance(s1.urlHashes, s2.urlHashes, eps) < eps;
//    }

    public static int[] jaccardDistanceBound(long sessionLength, int eps) {
        int[] bound = new int[2];
        // eps / 100 = 1 - bound / sessionLength;
        bound[0] = (int)((1 - eps / 100.0) * sessionLength) - 1;
        // eps / 100 = 1 - sessionLength / bound
        bound[1] = (int)(sessionLength / (1 - eps / 100.0) + 1);

        return bound;
    }

    /**
     * assume both s1 and s2 are sorted
     **/
    public static int jaccardDistance(long[] s1, long[] s2) {
        int countUnion = 0;
        for (long u1 : s1) {
            if (Arrays.binarySearch(s2, u1) >= 0)
                countUnion++;
        }

        return 100 - ((countUnion * 100) / (s1.length - countUnion + s2.length));
    }

    public static int jaccardDistance(Collection<Long> s1, Collection<Long> s2) {
        int countUnion = 0;
        for (Long u1 : s1) {
            if (s2.contains(u1))
                countUnion++;
        }

        return 100 - ((countUnion * 100) / (s1.size() - countUnion + s2.size()));
    }

    public static int editDistance(long[] s1, long[] s2, int upperLimit) {
        int len1 = s1.length;
        int len2 = s2.length;

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
            long c1 = s1[i];
            for (int j = 0; j < len2; j++) {
                long c2 = s2[j];

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    int min = dp[i][j];

                    dp[i + 1][j + 1] = min;
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;

                    if (min >= upperLimit)
                        return min;

                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    public static int core_distance(ArrayList<Neighbor> neighbors, int Minpts) {
        if (neighbors.size() < Minpts)
            return SessionClosed.UNDEFINED;

        int[] dists = new int[Minpts];
        for (int i = 0; i < dists.length; i++)
            dists[i] = neighbors.get(i).dist;

        for (int i = dists.length; i < neighbors.size(); i++) {
            Neighbor b = neighbors.get(i);

            for (int j = dists.length - 1; j >= 0; j--) {
                if (dists[j] > b.dist) {
                    dists[j] = b.dist;
                    Arrays.sort(dists);
                    break;
                }
            }
        }

        return dists[Minpts - 1];
    }

    private static final Comparator<SessionClosed> rdCmpr = new Comparator<SessionClosed>() {
        @Override
        public int compare(SessionClosed o1, SessionClosed o2) {
            return o1.reachability_distance - o2.reachability_distance;
        }
    };

    public static void update(ArrayList<Neighbor> N, ArrayList<SessionClosed> Seeds, int Minpts) {
        int coredist = core_distance(N, Minpts);
        for (Neighbor o : N) {
            if (!o.session.processed) {
                int new_reach_dist = Math.max(coredist, o.dist);

                if (o.session.reachability_distance == SessionClosed.UNDEFINED) {
                    // New sample which is reachable from sample p
                    o.session.reachability_distance = new_reach_dist;
                    Seeds.add(o.session);
                } else if (new_reach_dist < o.session.reachability_distance) {
                    // Already processed sample, now update its reachability distance
                    o.session.reachability_distance = new_reach_dist;
                }

            }
        }
    }

    public ArrayList<SessionClosed> orderedList;

    public void cluster() {
        //final double epsFactor = 0.3;
        final int eps = 20;
        final int Minpts = 5;

        orderedList = new ArrayList<SessionClosed>(sessionsClosed.length);

        System.out.println("Total sessions: " + sessionsClosed.length);

        int totalProcessed = 0;
        do {
            totalProcessed = 0;

            for (SessionClosed p : sessionsClosed) {
                if (p.processed) continue;

                if (totalProcessed % 10000 == 0)
                    System.out.println(totalProcessed);

                ArrayList<Neighbor> N = getNeighbors(p, eps);

                p.processed = true;
                orderedList.add(p);

                totalProcessed++;

                if (core_distance(N, Minpts) != SessionClosed.UNDEFINED) {
                    ArrayList<SessionClosed> Seeds = new ArrayList<SessionClosed>(Minpts);
                    update(N, Seeds, Minpts);

                    for (int i = 0; i < Seeds.size(); i++) {
                        SessionClosed q = Seeds.get(i);
                        ArrayList<Neighbor> N1 = getNeighbors(q, eps);

                        q.processed = true;
                        orderedList.add(q);
                        totalProcessed++;

                        if (core_distance(N1, Minpts) != SessionClosed.UNDEFINED) {
                            update(N1, Seeds, Minpts);
                        }
                    }
                }
            }

            System.out.println(totalProcessed);
        } while (totalProcessed > 0);
    }


    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("Free Mem: " + format.format(freeMemory) + "\n");
        sb.append("Alloc. Mem: " + format.format(allocatedMemory / 1024) + "\n");
        sb.append("Max Mem: " + format.format(maxMemory) + "\n");
        sb.append("Total free mem: " + format.format(freeMemory + (maxMemory - allocatedMemory)) + "\n");

        System.out.print(sb.toString());
    }

    public void parseFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));

        int i = 0;
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                Log log = parseLine(line);
                if (log == null || log.values_[6] == null
                    || log.values_[10].startsWith("10.") || log.values_[10].startsWith("192.168.") )
                    continue;

                String id = log.hasPreciseIDs()? log.getPreciseIDs() : log.getRoughIDs();

                Session session = tmp.sessions.get(id);
                if (session == null) {
                    session = new Session(id);
                    tmp.sessions.put(id, session);
                }

//                String ext = log.values_[6].toLowerCase();
//                if (ext.endsWith(".jpg") || ext.endsWith(".gif") || ext.endsWith(".js"))
//                    continue;

                Long hash = HashUtil.hash(log.values_[6]);
//                if (session.urlHashes.indexOf(hash) < 0)
                    session.urlHashes.add(hash);

//                if (i++ == 100000) {
//                    printMemoryUsage();
//                    i = 0;
//                }
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    public void printSessions() throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream("sessions.hashes"));

        HashMap<Long, Integer> url2id = new HashMap<Long, Integer>();
        int id = 0;

        for (SessionClosed p : sessionsClosed) {
            for (Long l : p.urlHashes) {
                Integer i = url2id.get(l);
                if (i == null)
                    url2id.put(l, id++);
            }
        }

        int[] row = new int[id];

        for (SessionClosed p : sessionsClosed) {
            Arrays.fill(row, 0);

            for (Long l : p.urlHashes) {
                int i = url2id.get(l);
                row[i] = row[i] + 1;
            }

            for (int i : row) {
                out.print(i);
                out.print(' ');
            }

            out.println();
        }

        out.close();
    }

    public static void main(String[] args) throws Exception {
        File path = new File(args[0]);

        SessionCluster splitter = new SessionCluster();
        splitter.setIDFields(true, "c-ip");
        splitter.setIDFields(false, "cs(Cookie)/[iI][dD]=([0-9.]*-[0-9]*)");
        splitter.setIDFields(false, "cs(Cookie)/id=([0-9a-f]+):");
        File[] children = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log.su");
            }
        });

        printMemoryUsage();
        Stopwatch watch = new Stopwatch();
        watch.start();

        for (File child : children) {
            System.out.println(child.getPath());

            splitter.parseFile(child);
        }

        splitter.closeAllSessions();

        splitter.printSessions();


//        printMemoryUsage();
//
//        System.out.println("Total time: " + watch.stop());
//        System.out.println("---------------Start Clustering ------------------------");
//
//        splitter.cluster();
//
//        System.out.println("---------------Done Clustering ------------------------");
//        System.out.println("Total time: " + watch.stop());
//
//        PrintStream out = new PrintStream(new FileOutputStream("sessions.txt"));
//
//        for (SessionClosed s : splitter.orderedList) {
//            out.print(((s.reachable == SessionClosed.UNDEFINED) ? "UNDEFINED" : s.reachable));
//            out.print(' ');
//
//            out.print(s.length);
//            out.print(' ');
//            out.print(Long.toHexString(s.sequenceHash));
//            out.print(' ');
//
//            for (String id : s.ids) {
//                out.print(id);
//                out.print(' ');
//            }
//
//            out.println();
//        }
//
//        out.close();
    }

}
