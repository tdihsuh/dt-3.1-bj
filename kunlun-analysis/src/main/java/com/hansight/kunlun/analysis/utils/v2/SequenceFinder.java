package com.hansight.kunlun.analysis.utils.v2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zhaoss on 14-10-13.
 */
public class SequenceFinder {
    static class Sequence {
        public final String uri_stem;
        public int count;

        public Sequence(String uri_stem) {
            this.uri_stem = uri_stem;
            this.count = 0;
        }

    } // Sequence

    HashMap<String, ArrayList<Sequence>> sequenceMap = new HashMap<String, ArrayList<Sequence>>();

    public void parseFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));

        int i = 0;
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (line.startsWith("#")) continue;
                String[] values = line.split(" ");
                if (values == null || values[10].startsWith("10.") || values[10].startsWith("192.168."))
                    continue;

                String cs_uri_stem = values[6];
                String cs_referrer = values[14];

                final String host = "://pbsz.ebank.cmbchina.com/";
                int pos = cs_referrer.indexOf(host);
                if (pos <= 0) continue;

                cs_referrer = cs_referrer.substring(pos + host.length());
                pos = cs_referrer.lastIndexOf('?');
                if (pos > 0)
                    cs_referrer = cs_referrer.substring(0, pos);

                ArrayList<Sequence> sequence = sequenceMap.get(cs_referrer);
                if (sequence == null) {
                    sequence = new ArrayList<Sequence>();
                    sequenceMap.put(cs_referrer, sequence);
                }

                Sequence found = null;
                for (Sequence s : sequence) {
                    if (s.uri_stem.equalsIgnoreCase(cs_uri_stem)) {
                        found = s;
                        break;
                    }
                }

                if (found == null) {
                    found = new Sequence(cs_uri_stem);
                    sequence.add(found);
                }

                found.count++;
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
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

        SequenceFinder finder = new SequenceFinder();
        for (File child : children) {
            System.out.println(child.getPath());
            finder.parseFile(child);
        }
    }
}