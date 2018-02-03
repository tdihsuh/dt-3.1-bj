package com.hansight.kunlun.analysis.utils.v2;


import com.hansight.kunlun.analysis.statistics.model.Log;
import com.hansight.kunlun.analysis.statistics.model.SessionSplitter;

import java.io.*;
import java.util.HashMap;

/**
 * Created by zhaoss on 14-10-15.
 */
public class SessionDocCluster extends SessionSplitter {
    HashMap<Long, DocumentVector> sessions = new HashMap<Long, DocumentVector>();

    public static long ip2Long(String ipstr) {
        // Parse IP parts into an int array
        int[] ip = new int[4];
        String[] parts = ipstr.split("\\.");

        for (int i = 0; i < 4; i++) {
            ip[i] = Integer.parseInt(parts[i]);
        }

        // Add the above IP parts into an int number representing your IP
        // in a 32-bit binary form
        long ipNumbers = 0;
        for (int i = 0; i < 4; i++) {
            ipNumbers += ip[i] << (24 - (8 * i));
        }

        return ipNumbers;
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

                // String id = log.hasPreciseIDs()? log.getPreciseIDs() : log.getRoughIDs();
                long cip = ip2Long(log.values_[10]);

                String ext = log.values_[6].toLowerCase();
                if (ext.endsWith(".jpg") || ext.endsWith(".gif") || ext.endsWith(".js"))
                    continue;

                DocumentVector session = sessions.get(cip);
                if (session == null) {
                    session = new DocumentVector();
                    sessions.put(cip, session);
                }

                session.incCount(log.values_[6]);
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    public void calculateSimMatrix() {
        Long[] keys = sessions.keySet().toArray(new Long[0]);

        System.out.println("Total sessions " + keys.length);

        for (int i = 0; i < keys.length; i++) {
            Long key1 = keys[i];
            DocumentVector session1 = sessions.get(key1);
            if (session1.count() < 4)
                continue;

            if (i % 1000 == 0)
                System.out.println(i);

            for (int j = i + 1; j < keys.length; j++) {
                Long key2 = keys[j];
                DocumentVector session2 = sessions.get(key2);
                if (session2.count() < 4)
                    continue;

                double sim = session1.getCosineSimilarityWith(session2);

                if (sim >= 0.8) {
//                    out.print(key1);
//                    out.print(' ');
//                    out.print(key2);
//                    out.print(' ');
//
//                    out.println((int) (sim * 100));
                }
            }
        }

    }


    public static void main(String[] args) throws Exception {
        File path = new File(args[0]);

        SessionDocCluster splitter = new SessionDocCluster();
        splitter.setIDFields(true, "c-ip");
        splitter.setIDFields(false, "cs(Cookie)/[iI][dD]=([0-9.]*-[0-9]*)");
        splitter.setIDFields(false, "cs(Cookie)/id=([0-9a-f]+):");
        File[] children = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log.su");
            }
        });

        for (File child : children) {
            System.out.println(child.getPath());

            splitter.parseFile(child);
        }

        System.out.println("---------------Start Calculating Distance ------------------------");

        PrintStream out = new PrintStream(new FileOutputStream("cosine.txt"));

        String[] keys = splitter.sessions.keySet().toArray(new String[0]);

        System.out.println("Total sessions " + keys.length);

        for (int i = 0; i < keys.length; i++) {
            String key1 = keys[i];
            DocumentVector session1 = splitter.sessions.get(key1);
            if (session1.count() < 4)
                continue;

            if (i % 1000 == 0)
                System.out.println(i);

            for (int j = i + 1; j < keys.length; j++) {
                String key2 = keys[j];
                DocumentVector session2 = splitter.sessions.get(key2);
                if (session2.count() < 4)
                    continue;

                double sim = session1.getCosineSimilarityWith(session2);

                if (sim >= 0.5) {
                    out.print(key1);
                    out.print(' ');
                    out.print(key2);
                    out.print(' ');

                    out.println((int) (sim * 100));
                }
            }
        }

        out.close();
    }



}
