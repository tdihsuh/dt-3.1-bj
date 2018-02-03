package com.hansight.kunlun.analysis.utils;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Created by zhhuiyan on 14/11/28.
 */
public class SearchRequestBuilderMaker {

    public static SearchRequestBuilder make(TransportClient client,String [] indices,String [] types){

        return client.prepareSearch(indices).setTypes(types);
    }
}
