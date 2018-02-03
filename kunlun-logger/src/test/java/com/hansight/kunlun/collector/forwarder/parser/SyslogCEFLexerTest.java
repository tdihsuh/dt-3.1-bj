package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.exception.CollectorException;
import com.hansight.kunlun.collector.lexer.CEFLogLexer;
import org.junit.Test;

import java.util.Map;

/**
 * Created by gavin_lee on 14-10-28.
 */
public class SyslogCEFLexerTest {
    @Test
    public void parse() throws CollectorException {
      String []  cases={"CEF:0|Check Point|VPN-1 & FireWall-1||accept|accept|Low| eventId=6566519872 mrt=1414641437319 proto=TCP customerID=SKLINiUYBABCAC9+TSjb5iA\\=\\= customerURI=/所有客户/北京分行 categorySignificance=/Normal categoryBehavior=/Access categoryDeviceGroup=/Firewall catdt=Firewall categoryoutcome=/Success categoryobject/Host/Application/Service modelConfidence=0 severity=4 relevance=10 accetCriticality=0 sourceZoneID=M1CLU5fsAABCCa7v-GNArfg\\=\\= sourceZoneURI=/所有区域/ArcSight 系统/公用位址空间区域/国防情报系统机构 3 sourceZoneExternalID=Defense Information System Agency 3 salt=39.96119 sourceGeoPostalCode=43218 source, host:null, charset:UTF-8"};
      //  String [] cases = {"CEF:0|ArcSight|ArcSight|7.0.1.6992.0|agent:012|Device Receipt Time from [estamp_prd\\|10.130.7.37\\|IBM\\|AIX Audit PR] may be incorrect - Device Receipt Time is greater than Agent Receipt Time (Events are in the future)|"};
        CEFLogLexer lexer=    new CEFLogLexer();
        for(int i=0;i<cases.length;i++){
            Map<String, Object> log= lexer.parse(cases[i]);
            for(Map.Entry entry:log.entrySet()){
                System.out.println("case["+i+"]:" + entry.getKey()+":"+entry.getValue());
            }
            assert log.size()>0;
        }


    }

}
