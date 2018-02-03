package com.hansight.kunlun.analysis.statistics.single;

import com.hansight.kunlun.analysis.statistics.model.FieldPattern;
import com.hansight.kunlun.analysis.statistics.model.Log;
import com.hansight.kunlun.analysis.statistics.model.SessionSplitter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SessionSplitterTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRegularExpression() throws Exception {
        FieldPattern pattern = FieldPattern.Parse(false, "cs(Cookie)/id=([0-9a-f]+):");
        assertEquals("2d825c060e7341472d71386685923239", pattern.match("cc_cmbchina_com_shop_city=0755;+cc_cmbchina_com_shop_cityname=%u6DF1%u5733;+AuthType=;+WTFPC=id=2d825c060e7341472d71386685923239:lv=1386685923239:ss=1386685923239"));

        pattern = FieldPattern.Parse(false, "cs(Cookie)/[iI][dD]=([0-9.]*-[0-9]*)");
        assertEquals("222.77.22.235-1083435632", pattern.match("UniProc1378975827=133866040880362500;+cmbuser_ID=222.77.22.235-1083435632.30286633::4A9D62BFDCA5376CE14AF38E93DEE4B3;+WTFPC=id=222.77.22.235-1083435632.30286633%3A%3A4A9D62BFDCA5376CE14AF38E93DE:lv=1386604141828:ss=1386604141828;+AuthType=A"));
    }

    protected SessionSplitter newSplitter() {
        SessionSplitter splitter = new SessionSplitter();
        splitter.setIDFields(true, "c-ip");
        splitter.setIDFields(false, "cs(Cookie)/[iI][dD]=([0-9.]*-[0-9]*)");
        splitter.setIDFields(false, "cs(Cookie)/id=([0-9a-f]+):");

        return splitter;
    }

    @Test
    public void testFieldPattern() throws Exception {
        SessionSplitter splitter = newSplitter();

        assertNull(splitter.parseLine("#Fields: date time s-sitename s-computername s-ip cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) cs-host sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken"));

        Log log = splitter.parseLine("2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_CreditCardV2/UI/Base/doc/Scripts/FuncTrigger.js - 443 - 58.254.4.107 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+6.1;+Trident/5.0;+SLCC2;+.NET+CLR+2.0.50727;+.NET+CLR+3.5.30729;+.NET+CLR+3.0.30729;+Media+Center+PC+6.0;+InfoPath.2;+Tablet+PC+2.0) AuthType=B https://pbsz.ebank.cmbchina.com/CmbBank_CreditCardV2/UI/CreditCardPC/CreditCardV2/am_QueryAccount.aspx pbsz.ebank.cmbchina.com 200 0 0 5957 573 0");
        assertEquals("58.254.4.107", log.getRoughIDs());
        assertEquals("", log.getPreciseIDs());

        log = splitter.parseLine("2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/PBPC/DebitCard_AccountManager/doc/Scripts/button.htc - 443 - 222.125.32.90 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+6.0;+SLCC1;+.NET+CLR+2.0.50727;+Media+Center+PC+5.0;+.NET+CLR+3.0.04506;+InfoPath.2) cmbuser_ID=222.125.32.90-35534768.30340342;+AuthType=A;+WTFPC=id=222.125.32.90-35534768.30340342:lv=1386604671701:ss=1386604139330 - pbsz.ebank.cmbchina.com 404 0 3 1496 481 0");
        assertEquals("222.125.32.90", log.getRoughIDs());
        assertEquals("222.125.32.90-35534768", log.getPreciseIDs());
    }

    @Test
    public void testFindMissingSecondaryIDs() throws Exception {
        String[] lines = {
        "#Fields: date time s-sitename s-computername s-ip cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) cs-host sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/Base/doc/Images/MessageNew9.gif - 443 - 121.205.10.90 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+8.0;+Windows+NT+5.1;+Trident/4.0;+SV1;+.NET+CLR+2.0.50727;+.NET+CLR+3.0.4506.2152;+.NET+CLR+3.5.30729) UniProc1378975827=133866040880362500;+cmbuser_ID=222.77.22.235-1083435632.30286633::4A9D62BFDCA5376CE14AF38E93DEE4B3;+WTFPC=id=222.77.22.235-1083435632.30286633%3A%3A4A9D62BFDCA5376CE14AF38E93DE:lv=1386604141828:ss=1386604141828;+AuthType=A https://pbsz.ebank.cmbchina.com/CmbBank_PB/UI/PBPC/DebitCard_AccountManager/am_QueryHistoryTrans.aspx pbsz.ebank.cmbchina.com 200 0 0 1351 695 0",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_Invest/UI/Base/DefaultDiv.htm - 443 - 125.74.11.14 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+5.1;+Trident/4.0;+GTB0.0;+InfoPath.2) AuthType=B https://pbsz.ebank.cmbchina.com/CmbBank_Invest/UI/InvestPC/Gold/CheckRisk.aspx pbsz.ebank.cmbchina.com 200 0 0 550 385 0",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_CreditCardV2/UI/Base/doc/Scripts/FuncTrigger.js - 443 - 58.254.4.107 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+6.1;+Trident/5.0;+SLCC2;+.NET+CLR+2.0.50727;+.NET+CLR+3.5.30729;+.NET+CLR+3.0.30729;+Media+Center+PC+6.0;+InfoPath.2;+Tablet+PC+2.0) AuthType=B https://pbsz.ebank.cmbchina.com/CmbBank_CreditCardV2/UI/CreditCardPC/CreditCardV2/am_QueryAccount.aspx pbsz.ebank.cmbchina.com 200 0 0 5957 573 0",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/PBPC/DebitCard_AccountManager/doc/Scripts/button.htc - 443 - 125.74.11.14 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+5.1;+Trident/4.0;+GTB0.0;+InfoPath.2) cmbuser_ID=222.125.32.90-35534768.30340342;+AuthType=A;+WTFPC=id=222.125.32.90-35534768.30340342:lv=1386604671701:ss=1386604139330 - pbsz.ebank.cmbchina.com 404 0 3 1496 481 0",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 POST /CmbBank_Invest/UI/InvestPC/Gold/GoldXmlQuery.aspx - 443 - 219.236.21.209 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+8.0;+Windows+NT+6.1;+WOW64;+Trident/4.0;+SLCC2;+.NET+CLR+2.0.50727;+.NET+CLR+3.5.30729;+.NET+CLR+3.0.30729;+.NET4.0C;+.NET4.0E;+InfoPath.3;+MS-RTC+LM+8) cmbuser_ID=219.236.21.209-2849705952.30337713::FDD8E7C0B79121941D1306A5AC74470E;+WTFPC=id=219.236.21.209-2849705952.30337713%3A%3AFDD8E7C0B79121941D1306A5AC7:lv=1386600225792:ss=1386600225792;+AuthType=A https://pbsz.ebank.cmbchina.com/CmbBank_Invest/UI/InvestPC/Gold/Index.aspx pbsz.ebank.cmbchina.com 200 0 0 12831 939 0",
        "2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/Base/doc/Scripts/DynaForm.js - 443 - 118.186.203.229 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+7.0;+Windows+NT+5.1;+Trident/4.0;+.NET+CLR+1.1.4322;+.NET+CLR+2.0.50727;+InfoPath.1;+.NET+CLR+3.0.4506.2152;+.NET+CLR+3.5.30729;+.NET4.0C) AuthType=B https://pbsz.ebank.cmbchina.com/CmbBank_PB/UI/Base/cn/Page/MessagePage4.aspx pbsz.ebank.cmbchina.com 200 0 0 5666 473 0"
        };

        SessionSplitter splitter = newSplitter();
/*
        for (String line : lines) {
            Log log = splitter.parseLine(line);
            if (log != null) splitter.logs_.add(log);
        }

        assertEquals("125.74.11.14", splitter.logs_.get(1).getRoughIDs());
        assertEquals("", splitter.logs_.get(1).getPreciseIDs());

        splitter.findMissingSecondaryIDs();

        assertEquals("125.74.11.14", splitter.logs_.get(1).getRoughIDs());
        assertEquals("222.125.32.90-35534768", splitter.logs_.get(1).getPreciseIDs());*/
    }
}
