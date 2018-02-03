package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.utils.ESIndexMaker;
import com.hansight.kunlun.collector.lexer.DelimiterLogLexer;
import com.hansight.kunlun.collector.lexer.RegexLogLexer;
import com.hansight.kunlun.utils.Pair;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Author:zhhui
 * DateTime:2014/7/25 18:00.
 */
public class LogParserTest {
    @Test
    public void testIISLogLexer() throws Exception {
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer();
        lexer.setTemplet("%{TIMESTAMP_ISO8601:@timestamp} (%{WORD:s_site_name})? (%{HOST:s_computer_name})? (%{IP:s_ip})? %{WORD:cs_method} %{URIPATH:cs_uri_stem} (%{NOTSPACE:cs_uri_query})? (%{NUMBER:s_port})? (%{NOTSPACE:cs_username})? %{IP:c_ip} %{NOTSPACE:cs_version} %{NOTSPACE:cs_useragent} (%{COOKIE:cs_cookie})? %{NOTSPACE:cs_referer} (%{NOTSPACE:cs_host})? %{NUMBER:sc_status} %{NUMBER:sc_substatus} %{NUMBER:sc_winstatus} %{NUMBER:sc_bytes} (%{NUMBER:cs_bytes})? %{NUMBER:time_taken}");
        Event event = new Event();
        Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
        event.setHeader(header);
        //event.setBody(ByteBuffer.wrap("2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/Base/doc/Images/MessageNew9.gif - 443 - 121.205.10.90 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+8.0;+Windows+NT+5.1;+Trident/4.0;+SV1;+.NET+CLR+2.0.50727;+.NET+CLR+3.0.4506.2152;+.NET+CLR+3.5.30729) UniProc1378975827=133866040880362500;+cmbuser_ID=222.77.22.235-1083435632.30286633::4A9D62BFDCA5376CE14AF38E93DEE4B3;+WTFPC=id=222.77.22.235-1083435632.30286633%3A%3A4A9D62BFDCA5376CE14AF38E93DE:lv=1386604141828:ss=1386604141828;+AuthType=A https://pbsz.ebank.cmbchina.com/CmbBank_PB/UI/PBPC/DebitCard_AccountManager/am_QueryHistoryTrans.aspx pbsz.ebank.cmbchina.com 200 0 0 1351 695 0".getBytes()));

        //test-1:
        //event.setBody(ByteBuffer.wrap("2014-10-29 15:59:59 10.3.210.84 GET /MobileHtml/Base/doc/images/home_cancel.png 443 183.194.52.166 HTTP/1.1 Mozilla/5.0+(Linux;+U;+Android+2.3.6;+zh-cn;+GT-N7000+Build/GINGERBREAD)+AppleWebKit/533.1+(KHTML,+like+Gecko)+Version/4.0+Mobile+Safari/533.1+MicroMessenger/6.0.0.54_r849063.501+NetType/WIFI https://mobile.cmbchina.com/MobileHtml/Login/HomePage.aspx?ClientNo=D08A4A1EC2853CA7243D2C5A33ECED9D066809315246795800137890 200 836 828 0".getBytes()));
        //test-2：
        //event.setBody(ByteBuffer.wrap("2014-10-29 15:59:59 GET /MobileHtml/Base/doc/images/button_back.png 218.18.49.254 HTTP/1.1 Mozilla/5.0+(Linux;+U;+Android+4.1.1;+zh-cn;+GT-N7100+Build/JRO03C)+AppleWebKit/534.30+(KHTML,+like+Gecko)+Version/4.0+Mobile+Safari/534.30 https://mobile.cmbchina.com/MobileHtml/DebitCard/AccountManage/AM_QueryTransaction.aspx 200 0 5078 0".getBytes()));
        //test-3:
        //event.setBody(ByteBuffer.wrap("2014-10-29 15:59:59 POST /netpayment/BaseHttp.dll 182.201.201.201 HTTP/1.1 Mozilla/5.0+(Linux;+U;+Android+4.3;+zh-cn;+N5117+Build/JLS36C)+AppleWebKit/534.30+(KHTML,+like+Gecko)+Version/4.0+Mobile+Safari/534.30 https://netpay.cmbchina.com/netpayment/BaseHttp.dll?23559h4xwYbM9FDxqgyE 200 4405 546".getBytes()));
        //test-4:
        //event.setBody(ByteBuffer.wrap("2014-10-29 16:00:00 POST /CmbBank_PB/UI/PBPC/Login/LoginGB.aspx 183.235.254.41 HTTP/1.1 CMB+HttpRequest+for+Win32 - 200 547 31".getBytes()));
        //test-5:
        event.setBody(ByteBuffer.wrap("2014-10-29 15:59:59 W3SVC1 PBSZ-D 10.0.10.13 POST /CmbBank_PB/UI/PBPC/DebitCard_NetPayManager/np_NetPayHomePage.aspx - 443 - 183.14.248.237 HTTP/1.1 Mozilla/5.0+(Windows+NT+6.1;+WOW64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/36.0.1985.125+Safari/537.36 WEBTRENDS_ID=219.137.51.204-1489402528.30397157::323FDA4E73499FBCC75B630EC74;+WTFPC=id=2571fd8a93a2cefcc161414595660974:lv=1414598317786:ss=1414598263550;+AuthType=A;+DeviceType=A https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenIndex.aspx pbsz.ebank.cmbchina.com 200 0 0 28098 898 0".getBytes()));



        Queue<Event> events = new ConcurrentLinkedDeque<>();
        events.add(event);
        lexer.setValueToParse(events);

        Map<String, Object> log = lexer.parse(events.poll());
        System.out.println("log.toJSONString() = " + log);
        /*ESDao<Map<String, Object>> writer = new DefaultESDao();
        writer.setIndex("logs_test");
        writer.setType("log_iis");
        writer.save(log);*/


    }
/*
    @Test
    public void testCSVLogLexer() throws Exception {
        Path path = Paths.get("F:/workspace/logger/data/csv/AdHoc_2014_04_01.csv");
        assert path != null;
        File file = path.toFile();
        assert file.exists() && file.isFile() && file.canRead();
        Lexer<Event, Map<String, Object>> lexer = new DelimiterLogLexer("#,@timestamp,generated_date,product_entity,product,product_ip,product_mac,management_server,malware,endpoint,s_host,user_name,handling,results,detections,entry_type,details".split(","), "\\t");
        //      BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "unicode"));
       *//* CsvReader csvReader = new CsvReader(new FileInputStream(file), '\t', Charset.forName("unicode"));
        if(csvReader.readHeaders()){
            String[] header = csvReader.getHeaders();
            System.out.println("header = " + header);
        }


        while (csvReader.readRecord()){
            String record = csvReader.getRawRecord();
            System.out.println("record = " + record);
        }
*/

    @Test
    public void testCSVLogLexer() throws Exception {
        Path path = Paths.get("F:\\data\\csv\\cmb-sniffer.csv");
        assert path != null;
        File file = path.toFile();
        assert file.exists() && file.isFile() && file.canRead();
        Lexer<Event, Map<String, Object>> lexer = new DelimiterLogLexer();
        lexer.setTemplet("\"segment,@timestamp,date,time,c_ip,s_ip,c_port,s_port,time_taken,cs_method,sc_status,rsp_text,cs_bytes,cs_host,cs_uri_stem,url,request_via,pragma,transfer_encoding,cs_useragent,cs_referer,accept,accept_charset,accept_encoding,accept_language,expect,from,server,sc_bytes,load_time,keep_alive\".split(\",\"), \"\\t\"");
        //      BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line = reader.readLine();
        int i = 0;
        while (line != null) {
            System.out.println("line = " + line);
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.parse(event);
            // ESDao<Map<String, Object>> writer = new DefaultESDao();
            Pair<String, Date> pair= new ESIndexMaker().indexSuffix(log);
            System.out.println("pair = " + pair);

           /* writer.setIndex("logs_"
                    + ESIndexMaker.indexSuffix(log));
            writer.setType("log_apache");
            writer.save(log);*/
            System.out.println(i++ + "log.toJSONString() = " + log);
            line = reader.readLine();
        }
    }

    @Test
    public void testSeparator() {
        String log = "#\t已接收\t已生成\t产品实体/端点\t产品\t产品/端点 IP\t产品/端点 MAC\t管理服务器实体\t病毒/恶意软件\t端点\t来源主机\t用户\t处理措施\t结果\t检测数\t 入口类型\t详细信息";
        String[] raws = log.split("\t");
        for (String raw : raws) {
            System.out.println("raw = " + raw);
        }
    }
/*
    @Test
    public void testApacheLogLexer() throws Exception {
        Path path = Paths.get("F:\\workspace\\logger\\data\\apache\\centos1-access_log-20140727");
        assert path != null;
        File reader = path.toFile();
        assert reader.exists() && reader.isFile() && reader.canRead();
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{APACHE}");
        BufferedReader reader = new BufferedReader(new FileReader(reader));
        String line = reader.readLine();
        int i = 0;
        while (line != null) {
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.call().poll();
            *//* ESDao<Map<String, Object>> writer = new DefaultESDao();
            writer.setIndex("logs_"
                    + ParserUtil.indexSuffix(log));
            writer.setType("log_apache");
            writer.save(log);*//*
            System.out.println(i++ + "log.toJSONString() = " + log);
            line = reader.readLine();
        }
    }

    @Test
    public void testRegexLogLexer() throws Exception {
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{COMBINEDAPACHELOG}");
        Event event = new Event();
        Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
        event.setHeader(header);
        event.setBody(ByteBuffer.wrap("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"".getBytes()));
        Queue<Event> events = new ConcurrentLinkedDeque<>();
        events.add(event);
        lexer.setValueToParse(events);

        Map<String, Object> log = lexer.call().poll();
        System.out.println("log.toJSONString() = " + log);
    }

    @Test
    public void testDefaultParser() throws Exception {
        Path path = Paths.get("F:\\workspace\\logger\\data\\iis\\ex131216.log");
        assert path != null;
        File reader = path.toFile();
        assert reader.exists() && reader.isFile() && reader.canRead();
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{IIS_LOG}");
        BufferedReader reader = new BufferedReader(new FileReader(reader));
        String line = reader.readLine();
        while (line != null) {
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.call().poll();
            if (log == null || log.isEmpty())
                System.out.println("line:" + line);
            line = reader.readLine();
        }

    }


    */
}
