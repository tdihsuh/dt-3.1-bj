package com.hansight.kunlun.analysis.statistics.single;

import com.hansight.kunlun.analysis.utils.SimHash;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;


class Distance {
    int distance_;
    String filename_;

    public Distance(int distance, File filename) {
        distance_ = distance;
        filename_ = filename.getName();
    }
}


/**
 * Created by justinwan on 14-7-8.
 */
public class SessionDistance {
    public static void calculateSimHashes(File folder) throws IOException {
        File children[] = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".session");
            }
        });

        for (File file : children) {
            final BufferedReader in = new BufferedReader(new FileReader(file));
            Iterator<String> pos = new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return fetchNext() != null;
                }

                private String next_;

                public String fetchNext() {
                    if (next_ == null) {
                        try {
                            next_ = in.readLine();
                        } catch (IOException ex) {
                            next_ = null;
                        }
                    }

                    return next_;
                }

                @Override
                public String next() {
                    String str = fetchNext();
                    next_ = null;

                    if (str != null) {
                        String[] fields = str.split("[ ]+");

                        if (fields[7].equals("-")) {
                            str = fields[6];
                        } else {
                            str = fields[6] + "?" + fields[7];
                        }
                    }

                    return str;
                }

                @Override
                public void remove() {
                    throw new IllegalArgumentException();
                }
            };

            BigInteger hash = SimHash.simHash(pos);

            in.close();

            String oldPath = file.getAbsolutePath();
            file.renameTo(new File(oldPath + "." + hash.toString(16)));
        }
    }

    public static BigInteger parseHash(File file) {
        String filename = file.getName();

        int pos = filename.indexOf(".session.");
        if (pos < 0) return null;

        BigInteger hash = new BigInteger(filename.substring(filename.lastIndexOf('.') + 1), 16);

        return hash;
    }


    public static void findNearest(File path) {
        final BigInteger hash1 = parseHash(path);
        if (null == hash1) {
            return;
        } else
            System.out.println(hash1.toString(16));

        final ArrayList<Distance> nearest = new ArrayList<Distance>(20);

        path.getParentFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(final File brother) {
                BigInteger hash2 = parseHash(brother);
                if (null == hash2) return false;

                if (hash1.equals(hash2)) return false;

                int dist = SimHash.hammingDistance(hash1, hash2);

                if (nearest.size() < 20) {
                    nearest.add(new Distance(dist, brother));
                } else {
                    for (int i = 0; i < nearest.size(); i++) {
                        Distance distance = nearest.get(i);
                        if (dist < distance.distance_) {
                            nearest.set(i, new Distance(dist, brother));
                            break;
                        }
                    }
                }

                return true;
            }

        });


        for (int i = 0; i < nearest.size(); i++) {
            Distance distance = nearest.get(i);
            System.out.println(distance.filename_ + " " + distance.distance_);
        }
   }


    public static void main(String[] argv) throws IOException {
        File path = new File(argv[0]);

        if (path.isDirectory()) {
            calculateSimHashes(path);
        } else {
            findNearest(path);
        }
    }

}
