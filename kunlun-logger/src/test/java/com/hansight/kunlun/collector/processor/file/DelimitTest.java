package com.hansight.kunlun.collector.processor.file;

import oi.thekraken.grok.api.exception.GrokException;
import org.junit.Assert;
import org.junit.Test;

import java.util.StringTokenizer;

/**
 * Author:zhhui
 * DateTime:2014/8/4 14:12.
 */
public class DelimitTest {
  @Test
  public void testDelimit() throws GrokException {


    String value = "2013-12-10 00:14:32 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_GenShell/UI/Base/doc/Scripts/DynaXmlHttp.js - 443 - 117.21.246.44 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+6.0;+Windows+NT+5.1;+SV1;+InfoPath.2) WTFPC=id=113.194.128.249-1143634944.30309436:lv=1386634427156:ss=1386634427156;+AuthType=A;+cmbuser_ID=113.194.128.249-1143634944.30309436 https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenIndex.aspx pbsz.ebank.cmbchina.com 200 0 0 4295 515 0";
    value= value.replaceAll(" ","   ");
  //  StringTokenizer tokenizer = new StringTokenizer(value, "\t");
    System.out.println("value = " + value);
      String [] values= value.split(" ");
      for(String v:values){
          System.out.println("v =|" + v.trim()+"|");
      }
    Assert.assertEquals("must be ", 22, value.split(" ").length);

  }
}
