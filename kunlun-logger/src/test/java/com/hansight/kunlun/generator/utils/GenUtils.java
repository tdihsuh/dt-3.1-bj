package com.hansight.kunlun.generator.utils;

import java.io.File;
import java.util.List;
import java.util.Random;

public class GenUtils
{
  public static long parseSizeWtihUnit(String tmp)
    throws Exception
  {
    long interval = 0L;
    switch (tmp.substring(tmp.length() - 1)) {
    case "g":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 1024L * 1024L * 1024L;
      break;
    case "m":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 1024L * 1024L;
      break;
    case "t":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 1024L * 1024L * 1024L * 1024L;
      break;
    default:
      throw new Exception("unknow log file size unit");
    }
    return interval;
  }

  public static long parseDateWithUnit(String tmp) throws Exception {
    long interval = 0L;
    switch (tmp.substring(tmp.length() - 1)) {
    case "h":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 60L * 60L * 1000L;
      break;
    case "m":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 60L * 1000L;
      break;
    case "s":
      interval = Long.parseLong(tmp.substring(0, tmp.length() - 1)) * 60L * 1000L;
      break;
    default:
    	interval = Long.parseLong(tmp);
    }
    return interval;
  }
  public static String randomTime(Random random) {
    String time = "";
    time = time + preChar("0", random.nextInt(24), 2);
    time = time + ":";
    time = time + preChar("0", random.nextInt(60), 2);
    time = time + ":";
    time = time + preChar("0", random.nextInt(60), 2);
    time = time + ".";
    time = time + preChar("0", random.nextInt(1000000), 6);
    return time;
  }

  public static String randomIP(Random random) {
    String time = "";
    time = time + (random.nextInt(253) + 1);
    time = time + ".";
    time = time + (random.nextInt(253) + 1);
    time = time + ".";
    time = time + (random.nextInt(253) + 1);
    time = time + ".";
    time = time + (random.nextInt(252) + 2);
    return time;
  }

  public static String join(String[] arr, String split, String end) {
    StringBuffer sb = new StringBuffer();
    sb.append(arr[0]);
    for (int i = 1; i < arr.length; i++) {
      sb.append(split).append(arr[i].trim());
    }
    sb.append(end);
    return sb.toString();
  }

  public static String preChar(String pre, int val, int count) {
    String suf = "";
    for (int i = String.valueOf(val).length(); i < count; i++) {
      suf = suf + pre;
    }
    suf = suf + val;
    return suf;
  }

  public static void listAll(File file, List<File> list) {
    File[] tmp = file.listFiles();
    if (tmp != null) {
    	for (File f : tmp)
    		if (f.isFile())
    			list.add(f);
    		else
    			listAll(f, list);
    }
  }

  public static String[] split(String line, String split)
  {
    int start = 0; int count = 0;
    while (true) {
      int index = line.indexOf(split, start);
      if (index == -1) {
        break;
      }
      start = index + 1;
      count++;
    }
    String[] tmp = new String[count + 1];
    start = count = 0;
    while (true) {
      int index = line.indexOf(split, start);
      if (index == -1) {
        tmp[count] = line.substring(start);
        break;
      }
      tmp[count] = line.substring(start, index);
      start = index + 1;
      count++;
    }

    return tmp;
  }

  public static void main(String[] args) {
    Random random = new Random();
    System.out.println(randomTime(random));
    String tmp = "SZ\t1414166695.520011\t2014-11-20\t01:02:33.009857\t58.15.95.139\t218.17.246.171\t1473\t80\t2\tGET\t200\tOK\t       484\tcc.cmbchina.com\t/Scripts/sdc_web.js\thttp://cc.cmbchina.com/Scripts/sdc_web.js\t\t\t\t Mozilla/5.0 (Linux; U; Android 4.3; zh-cn; GT-I9508 Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30 MicroMessenger/5.3.1.51_r733746.462 NetType/WIFI\t http://market.cmbchina.com/ccard/wap/zssh40wx/le.html?utm_source=yd&utm_medium=own&utm_campaign=N37WL00011408110\t */*\t utf-8, iso-8859-1, utf-16, *;q=0.7\t gzip,deflate\t zh-CN, en-US\t\t\t Microsoft-IIS/6.0\t 484\t Fri, 24 Oct 2014 16:04:56 GMT\t";
    tmp = join(split(tmp, "\t"), "\t", "");
    System.out.println(split(tmp, "\t").length);

    String line = new String(tmp).replaceAll("\t", " \t ");
    String[] values = line.split("\t");
    System.out.println("re:" + values.length);
    System.out.println("\t1\t2\t\t5\t".split("\t").length);
  }
}