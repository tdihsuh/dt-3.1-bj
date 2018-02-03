package com.hansight.kunlun.analysis.statistics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.single.RTHandler;

public class TestMatch {
	public static void main(String[] args) {
		String regex = "(/[cC][mM][bB][bB][aA][nN][kK]_[\\d\\D]*)|([^\\.%'\"<>]*((\\.htc)|(\\.gif)|(\\.jpg)|(\\.js)|(\\.ico)|(\\.png)))";
		String regex2 = RTHandler.get(Constants.RT_FETCH_REFERENCE_REGEX, regex);
		System.out.println(regex.equals(regex));
		Pattern p = Pattern.compile(regex2);
		Matcher m = p.matcher("/CmbBank_GenShell/UI/GenShellPC/Login/&0.7101833646360618&0.8430420495622122");
		System.out.println(m.matches());
				
	}
}
