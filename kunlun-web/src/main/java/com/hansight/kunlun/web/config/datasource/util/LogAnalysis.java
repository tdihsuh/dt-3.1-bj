package com.hansight.kunlun.web.config.datasource.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import oi.thekraken.grok.api.Grok;
//import oi.thekraken.grok.api.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LogAnalysis {
	private static final Logger LOG = LoggerFactory.getLogger(LogAnalysis.class);
	public static List<Log> analysis(String type, String log) {
		List<Log> analysisLog = new ArrayList<Log>();
		Log logName = null;
		if(log.length()>0){
			if (type.equals("iis")) {
				String []logIS = log.split("\n");
				String [] logN = null;
				String [] logV = null;
				LOG.info("大小：：：："+logIS.length);
					logN = logIS[0].split("	");
					logV = logIS[1].split("	");
					if(logN.length>2&&logV.length>2){
						for (int i = 0; i < logN.length; i++) {
							logName = new Log(); 
							logName.setId(i+1);
							logName.setLogName(logN[i].toLowerCase());
							if(i<logV.length){
							logName.setLogValue(logV[i]);
							}
							analysisLog.add(logName);
						}
					}else{
						String[] logN1 = logIS[0].split(",");
						String[] logV1 = logIS[1].split(",");
						if(logN1.length>2){
							for (int i = 0; i < logN1.length; i++) {
								logName = new Log(); 
								logName.setId(i+1);
								logName.setLogName(logN1[i].toLowerCase());
								if(i<logV1.length){
								logName.setLogValue(logV1[i]);
								}
								analysisLog.add(logName);
							}
						}else{
							int num = log.indexOf("#Fields:");
							String logValue = log.substring(num+"#Fields:".length() ).trim();
							System.out.println(logValue);
							String []logISs = logValue.split("\n");
							String[] logN2 = logISs[0].split(" ");
							String[] logV2 = logISs[1].split(" ");
							System.out.println("logSize:"+logN2.length+"logISs:::::"+logISs.length);
							if(logN2.length>2){
								for (int i = 0; i < logN2.length; i++) {
									logName = new Log(); 
									logName.setId(i+1);
									logName.setLogName(logN2[i].toLowerCase());
									if(i<logV2.length){
									logName.setLogValue(logV2[i]);
									}
									analysisLog.add(logName);
								}
							}
						}
			
					}
		}else if (type.equals("csv")) {
					String []logCs = log.split("\n");
					LOG.info("大小：：：：：：：：："+logCs.length);
					String []logCn = null;
					String []logCv = null;
					if(logCs.length==2){
						logCn = logCs[0].split("	");
						logCv = logCs[1].split("	");
						if(logCn.length>2){
							for (int i = 0; i < logCn.length; i++) {
								logName = new Log();
								logName.setId(i+1);
								logName.setLogName(logCn[i]);
								if(i<logCv.length){
								logName.setLogValue(logCv[i]);
								}
								analysisLog.add(logName);
							}
						}else{
							String []logCn1 = logCs[0].split(",");
							String []logCv1 = logCs[1].split(",");
							for (int i = 0; i < logCn1.length; i++) {
								logName = new Log(); 
								logName.setId(i+1);
								logName.setLogName(logCn1[i].toLowerCase());
								if(i<logCv1.length){
								logName.setLogValue(logCv1[i]);
								}
								analysisLog.add(logName);
							}
						}
					}
			}else if (type.equals("other")){
				String []logElse = log.split("\n");
				LOG.info("logElse大小：：：：：：：：："+logElse.length);
					String[] logEn = logElse[0].split("	");
					String[] logEv = logElse[1].split("	");
					if(logEn.length>2){
						for (int i = 0; i < logEn.length; i++) {
							logName = new Log();
							logName.setId(i+1);
							logName.setLogName(logEn[i]);
							if(i<logEv.length){
							logName.setLogValue(logEv[i]);
							}
							analysisLog.add(logName);
						}
					}else{
						String []logEn1 = logElse[0].split(",");
						String []logEv1 = logElse[1].split(",");
						LOG.info("logEn1:::"+logEn1.length);
						if(logEn1.length>2){
							for (int i = 0; i < logEn1.length; i++) {
								logName = new Log(); 
								logName.setId(i+1);
								logName.setLogName(logEn1[i]);
								if(i<logEv1.length){
								logName.setLogValue(logEv1[i]);
								}
								analysisLog.add(logName);
							}
						}else{
							int num = log.indexOf("#Fields:");
							String logValue = log.substring(num+"#Fields:".length() ).trim();
							System.out.println(logValue);
							String []logOther = logValue.split("\n");
							String[] logO = logOther[0].split(" ");
							String[] logOv = logOther[1].split(" ");
							LOG.info("logO::::::::"+logO.length);
							if(logO.length>2){
								for (int i = 0; i < logO.length; i++) {
									logName = new Log(); 
									logName.setId(i+1);
									logName.setLogName(logO[i].toLowerCase());
									if(i<logOv.length){
									logName.setLogValue(logOv[i]);
									}
									analysisLog.add(logName);
								}
							}
						}
				}
			}
		}
		return analysisLog;
	}

	public static List<String> analysisUrl(String type, String path) {
		List<String> analysisLog = new ArrayList<>();
		File file = new File(path);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String logLine = reader.readLine();
			String[] log = null;
			if (type.equals("iis")) {
				log = logLine.split("	");
			} else if (type.equals("csv")) {
				log = logLine.split(",");
			}
			for (int i = 0; i < log.length; i++) {
				analysisLog.add(log[i]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return analysisLog;
	}

	public static void main(String[] args) {
//		String path = "F:/新日志/cmb.new/sniffer-iis/20141026050000.xls";
//		List<Log> analysisLog = new ArrayList<Log>();
//		File file = new File(path);
//		BufferedReader reader = null;
//		String[] logLine = new String [100];
//		String tempString = null;
//		int count = 0 ;
//		try {
//			reader = new BufferedReader(new FileReader(file));
//			 while ((tempString = reader.readLine()) != null) {
//	                //System.out.println( tempString);
//	                logLine[count++] = tempString;
//	                if(count==2){
//	                	break;
//	                }
//	          }
//			
//			String[] logN = null;
//			String[] logV = null;
//			for (int j = 0; j < logLine.length; j++) {
//				if(j==0)
//				 logN = logLine[j].split("	");
//				else if(j==1)
//				 logV = logLine[j].split("	");
//			}
//			System.out.println(logN.length+",,"+logV.length);
//			reader.close();
//			for (int k = 0; k< logN.length; k++) {
//				System.out.println(logN[k]);
//			}
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e1) {
//				}
//			}
//		}
//		int ou = 0;
//		ou = 60/2;
//		System.out.println(ou);
		String strd = "#Fields: date time s-sitename";
		int num = strd.indexOf("#Fields:");
		String is = strd.substring(num+"#Fields:".length() );
		int d = strd.indexOf("#Fields:");
		System.out.println(is);
		
		
		
	}

}
