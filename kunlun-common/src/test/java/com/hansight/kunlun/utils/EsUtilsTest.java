package com.hansight.kunlun.utils;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;



/**
 * Created by gavin_lee on 14/11/17.
 */
public class EsUtilsTest {

    @Test
    public void testEsConnection(){
        TransportClient client = EsUtils.getEsClient();
        SearchResponse scrollResp = client.prepareSearch("ipblack").setTypes("ipblack")
                .setSearchType(SearchType.SCAN)
                .setSize(10)
                .setScroll(TimeValue.timeValueMinutes(8)).execute().actionGet();


        while (true) {
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();
            for (SearchHit hit : scrollResp.getHits()) {
                System.out.println(hit.getId() + hit.getSourceAsString());
            }
            // Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

    }
}
