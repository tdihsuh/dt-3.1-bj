package com.hansight.kunlun.analysis.utils.v2;


import com.hansight.kunlun.analysis.statistics.model.Log;
import com.hansight.kunlun.analysis.statistics.model.SessionSplitter;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;


class SessionByCIP {
    public String c_ip;
    public HashSet<String> users = new HashSet<String>();

    public SessionByCIP(String cip_) {
        c_ip = cip_;
    }

    public boolean isSingleUser() {
        return users.size() == 0 || users.size() == 1;
    }
}


/**
 * Created by zhaoss on 14-10-8.
 */
public class SingleUserSessionSplitter extends SessionSplitter {
    protected HashMap<String, SessionByCIP> sessions = new HashMap<String, SessionByCIP>();

    public void parseFile(File file) throws IOException {
        // Pass 1
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                Log log = parseLine(line);
                if (log == null) continue;

                String cip = log.values_[10];
                SessionByCIP session = sessions.get(cip);
                if (session == null) {
                    session = new SessionByCIP(cip);
                    sessions.put(cip, session);
                }

                String pid = log.getPreciseIDs();
                if (pid != null && pid.length() > 0)
                    session.users.add(pid);
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }

        // Pass 2
        in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));
        PrintWriter out = new PrintWriter(new File(file.getPath() + ".su"), "iso-8859-1");
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (line.startsWith("#")) {
                    out.println(line);
                    continue;
                }

                Log log = parseLine(line);
                if (log == null) continue;

                String cip = log.values_[10];
                if (sessions.get(cip).isSingleUser()) {
                    out.println(line);
                }
            }

            out.close();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }


    public static void main(String[] args) throws Exception {
        File path = new File(args[0]);

        SingleUserSessionSplitter splitter = new SingleUserSessionSplitter();
        splitter.setIDFields(true, "c-ip");
        splitter.setIDFields(false, "cs(Cookie)/[iI][dD]=([0-9.]*-[0-9]*)");
        splitter.setIDFields(false, "cs(Cookie)/id=([0-9a-f]+):");
        File[] children = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });

        Stopwatch watch = new Stopwatch();
        watch.start();

        for (File child : children) {
            System.out.println(child.getPath());

            splitter.parseFile(child);
        }

//        PrintStream out = new PrintStream(new FileOutputStream("cip_users.txt"));
//
//        for (SessionByCIP s : splitter.sessions.values()) {
//            out.print(s.c_ip);
//            out.print(' ');
//
//            int count = s.users.size();
//            if (count == 0) count = 1;
//            out.print(count);
//            out.print(' ');
//
//            for (String id : s.users) {
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
