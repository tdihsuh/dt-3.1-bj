package com.hansight.kunlun.generator.sniffer;

import com.hansight.kunlun.generator.utils.GenUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateSnifferIIS2
{
  private static final Logger LOG = LoggerFactory.getLogger(GenerateSnifferIIS2.class);

  public static void main(String[] args) throws Exception
  {
    if (args.length != 6) {
      System.out.println("Usage:<log dir> <dist dir> <log size> <file count> <date str> <interval time>");

      System.out.println("<interval time> unit:h, m, s");
      System.out.println("example: /root/cmb.new/sniffer-iis /grid/0/gen-sniffer-iis 100g 1 2014-11-27 24h");

      return;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    File logDir = new File(args[0]);
    String distDir = args[1];
    long dataLen = GenUtils.parseSizeWtihUnit(args[2]);
    int fileCount = Integer.parseInt(args[3]);
    Date startDate = sdf.parse(args[4]);
    long interval = GenUtils.parseDateWithUnit(args[5]);

    int days = 0;
    while (true) {
      Date distDate = DateUtils.addDays(startDate, days);
      gen(distDate, logDir, distDir, fileCount, days, "UTF-8", dataLen);

      days++;
      LOG.info("over, sleep...");
      TimeUnit.MILLISECONDS.sleep(interval);
    }
  }

  public static void gen(Date distDate, File logDir, String distDir, int fileCount, int days, String encoding, long dataLen)
    throws IOException, ParseException
  {
    File[] logs = logDir.listFiles();
    long total = 0L;
    for (File file : logs) {
      total += file.length();
    }
    int times = (int)((dataLen + total - 1L) / total);
    int files = (times * logs.length + fileCount - 1) / fileCount;

    int uses = 0; int count = 0;
    BufferedWriter out = null;
    for (int i = 0; i < times; i++) {
      for (int j = 0; j < logs.length; j++) {
        if ((out == null) || (count >= files)) {
          if (out != null) {
            out.close();
          }
          File distFile = null;
          distFile = getOutFile(uses, distDir, distDate);
          out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(distFile), encoding));

          uses++;
          count = 0;
        }
        File file = logs[j];
        gen(file, out, 1, distDate, encoding);
        count++;
      }
    }
    out.close();
  }

  public static void gen(File file, BufferedWriter out, int headCounts, Date distDate, String encoding) throws IOException, ParseException
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

    int lineNumber = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
    Random random = new Random();
    String line;
    while ((line = in.readLine()) != null) {
      lineNumber++;
      if (lineNumber <= headCounts) {
        out.write(line);
        out.write("\n");
      } else {
        out.write(replace(line, sdf, sdf2, distDate, random));
      }
      out.flush();
    }
    in.close();
    LOG.debug("add file:{}", file.getName());
  }

  public static String replace(String line, SimpleDateFormat sdf, SimpleDateFormat sdf2, Date distDate, Random random)
    throws ParseException
  {
    String[] tmp = line.replaceAll("\t", " \t ").split("\t");
    tmp[2] = sdf.format(distDate);
    tmp[3] = GenUtils.randomTime(random);
    tmp[1] = new StringBuilder().append(String.valueOf(sdf2.parse(new StringBuilder().append(tmp[2]).append(tmp[3].substring(0, 8)).toString()).getTime() / 1000L + 2L)).append(tmp[1].substring(tmp[1].indexOf(46))).toString();

    return GenUtils.join(tmp, "\t", "\r\n");
  }

  public static File getOutFile(int cur, String distDir, Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    String pre = sdf.format(date);
    String suf = GenUtils.preChar("0", cur, 6);
    System.out.println(new StringBuilder().append("output:").append(distDir).append(distDir.endsWith("/") ? "" : "/").append(pre).append(suf).append(".xls").toString());

    return new File(new StringBuilder().append(distDir).append(distDir.endsWith("/") ? "" : "/").append(pre).append(suf).append(".xls").toString());
  }
}