package com.hansight.kunlun.analysis.utils.v2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhaoss on 14-10-5.
 */
public class SizeCounter {
    public String[] parseLine(String line) throws IOException {
        if (line.startsWith("#")) {
            return null;
        }

        String[] values_ = line.split(" ");

        for (int i = 0; i < values_.length; i++) {
            if (values_[i].equals("-")) values_[i] = null;
        }

        return values_;
    }

    static class Count {
        public final int size;
        public int count;

        public Count(int size_) {
            size = size_;
            count = 1;
        }
    }

    static class NoCaseString {
        public final String s;
        public final String sLowerCase;

        public NoCaseString(String s_) {
            s = s_;
            sLowerCase = s.toLowerCase();
        }

        @Override
        public boolean equals(Object o) {
            return sLowerCase.equals(o);
        }

        @Override
        public int hashCode() {
            return sLowerCase.hashCode();
        }
    }

    private HashMap<NoCaseString, ArrayList<Count>> counts = new HashMap<NoCaseString, ArrayList<Count>>();

    public void incr(String key) {
        incr(key, 0);
    }

    public void incr(String key, int size) {
        ArrayList<Count> list = counts.get(key);
        if (list == null) {
            list = new ArrayList<Count>();
            counts.put(new NoCaseString(key), list);
        }

        for (Count count : list) {
            if (count.size == size) {
                count.count++;
                return;
            }
        }

        Count count = new Count(size);
        list.add(count);
    }

    public void parseFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));

        int i = 0;
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] values = parseLine(line);

                if (values == null || values[10].startsWith("10.") || values[10].startsWith("192.168.") )
                    continue;

                if (++i % 100000 == 0) {
                    System.out.println(i);
                    SessionCluster.printMemoryUsage();
                }

                // only count HTTP 200 OK and
                String statusKey = values[16] + " " + values[17] + " " + values[18];
                incr(statusKey);

                if (!values[16].equals("200") || !values[17].equals("0") || !values[18].equals("0"))
                    continue;

                String key = values[5] + " " + values[6];
                incr(key, Integer.parseInt(values[19]));
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    public void dumpSize(PrintStream out) {
        Iterator<Map.Entry<NoCaseString, ArrayList<Count>>> pos = counts.entrySet().iterator();
        while (pos.hasNext()) {
            Map.Entry<NoCaseString, ArrayList<Count>> entry = pos.next();

            out.print(entry.getKey().s); out.print(' ');

            for (Count c : entry.getValue()) {
                out.print(c.size); out.print('/'); out.print(c.count);
            }

            out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        File path = new File(args[0]);

        File[] children = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });

        SizeCounter counter = new SizeCounter();

        for (File child : children) {
            System.out.println(child.getPath());

            counter.parseFile(child);
        }

        PrintStream out = new PrintStream(new FileOutputStream("size.out"));
        counter.dumpSize(out);
        out.close();
    }
}