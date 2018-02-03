package com.hansight.kunlun.analysis.statistics.single;

import java.io.IOException;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.ScriptService;

import com.hansight.kunlun.utils.EsUtils;

public class ESTest {
	public static void main(String[] args) throws ElasticsearchException, IOException, Exception {
		Client client = EsUtils.getEsClient();
		
		IndexResponse res = client.prepareIndex("logs_20141127", "anomaly", "2")
		.setSource(XContentFactory.jsonBuilder()
				.startObject()
				.field("counter", 1)
				.endObject()
				).execute().actionGet();
		
		UpdateResponse res2 = client.prepareUpdate("logs_20141127", "anomaly", "2")
				.setScript("ctx._source.counter += 1", ScriptService.ScriptType.INLINE).execute().actionGet();
		System.out.println(res2.isCreated());
	}
}
