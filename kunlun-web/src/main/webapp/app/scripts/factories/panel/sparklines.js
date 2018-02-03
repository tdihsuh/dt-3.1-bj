define(['app','scripts/factories/charts/sparklineCharts'],function(app,SparklineCharts){
	app.factory('PanelSparklines',['$http','SparklineCharts','hs_color','$filter',function($http,SparklineCharts,hs_color,$filter){
		return {
			generate:function(url,param,id_num,content,fromTime,currentTime){
				var interval;
				switch (content.rangeUnit) {
				case 'second':
					interval = "s";
					break;
				case 'minute':
					interval = "m";
					break;
				case 'hour':
					interval = "h";
					break;
				case 'day':
					interval = "d";
					break;
				case 'week':
					interval = "w";
					break;
				case 'month':
					interval = "m";
					break;
				case 'year':
					interval = "y";
					break;
				}
				if(content.aggsType == "field"){
					if(param.query.bool){
						if(!param.query.bool.must)
							param.query.bool["must"] = [];
						if(!param.query.bool.must.range)
							param.query.bool.must["range"] = [];
						if(!param.query.bool.must.range["@timestamp"])
							param.query.bool.must.range["@timestamp"] = [];
						param.query.bool.must.range["@timestamp"].from = fromTime;
						param.query.bool.must.range["@timestamp"].to = currentTime;
					}else{
						param.query.bool = {
								"must":[
								        {
								        	"range":{
								        		"@timestamp":{
								        			"from":fromTime,
								        			"to":currentTime
								        		}
								        	}
								        }
								]
						}
//						param.query.bool = {};
//						param.query.bool["must"] = [];
					}
					var s_term = {};
					s_term["term"] = {};
					s_term["term"][content.field] = content.fieldValue;
					param.query.bool.must.push(s_term);
					delete param.query["range"];
				}else{
					if(param.query.bool){
						param.query.bool.must[1].range["@timestamp"].from = fromTime;
						param.query.bool.must[1].range["@timestamp"].to = currentTime;
					}else{
						param.query.range["@timestamp"]["from"] = fromTime;
						param.query.range["@timestamp"]["to"] = currentTime;
					}
				}
				param["aggs"] = {
					"sparkline_field":{
						"date_histogram":{
							"field":"@timestamp",
							"interval":content.refresh+interval
						}
					}
				};
				param["size"] = 0;
				delete param["sort"];
				delete param["from"];
				
				$http.post(url,param).success(function(data){
					var width = $("#panel_content_"+id_num).width();
					var height = $("#panel_content_"+id_num).height();
					
					var datas = data.aggregations.sparkline_field.buckets;
					var ds = [];
					for(var i in datas){
						key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
						count = datas[i].doc_count;
						ds.push({"key":($filter('date')(key,"yyyy-MM-dd HH:mm:ss")),"count":count});
					}
					
					var sparklineCharts = new SparklineCharts();
					sparklineCharts.width = width;
					sparklineCharts.height = height;
					if(content.color == null || $.trim(content.color) == "")
						sparklineCharts.colors = hs_color;
					else
						sparklineCharts.colors = [content.color];
					sparklineCharts.num = id_num;
					sparklineCharts.showX = content.xAxis;
					sparklineCharts.showY = content.yAxis;
					sparklineCharts.legend = content.legend;
					if(content.showTitle)
						sparklineCharts.title = content.title;
					sparklineCharts.xRotate = content.xRotate;
					sparklineCharts.draw("#panel_content_"+id_num,ds);
				});
			},
			generates:function(url,param,id_num,content,fromTime,currentTime,alt,panel){
				var interval;
				switch (content.rangeUnit) {
				case 'second':
					interval = "s";
					break;
				case 'minute':
					interval = "m";
					break;
				case 'hour':
					interval = "h";
					break;
				case 'day':
					interval = "d";
					break;
				case 'week':
					interval = "w";
					break;
				case 'month':
					interval = "m";
					break;
				case 'year':
					interval = "y";
					break;
				}
				if(content.aggsType == "field"){
					if(param.query.bool){
						if(!param.query.bool.must)
							param.query.bool["must"] = [];
						if(!param.query.bool.must.range)
							param.query.bool.must["range"] = [];
						if(!param.query.bool.must.range["@timestamp"])
							param.query.bool.must.range["@timestamp"] = [];
						param.query.bool.must.range["@timestamp"].from = fromTime;
						param.query.bool.must.range["@timestamp"].to = currentTime;
					}else{
						param.query.bool = {
								"must":[
								        {
								        	"range":{
								        		"@timestamp":{
								        			"from":fromTime,
								        			"to":currentTime
								        		}
								        	}
								        }
								]
						}
//						param.query.bool = {};
//						param.query.bool["must"] = [];
					}
					var s_term = {};
					s_term["term"] = {};
					s_term["term"][content.field] = content.fieldValue;
					param.query.bool.must.push(s_term);
					delete param.query["range"];
				}else{
					if(param.query.bool){
						param.query.bool.must.range["@timestamp"].from = fromTime;
						param.query.bool.must.range["@timestamp"].to = currentTime;
					}else{
						param.query.range["@timestamp"]["from"] = fromTime;
						param.query.range["@timestamp"]["to"] = currentTime;
					}
				}
				param["aggs"] = {
					"sparkline_field":{
						"date_histogram":{
							"field":"@timestamp",
							"interval":content.refresh+interval
						}
					}
				};
				param["size"] = 0;
				delete param["sort"];
				delete param["from"];
				
				$http.post(url,param).success(function(data){
					
					var properties_name = "pro_";
					var panel_name = "charts_";
					
					
					var datas = data.aggregations.sparkline_field.buckets;
					var ds = [];
					for(var i in datas){
						key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
						count = datas[i].doc_count;
						ds.push({"key":($filter('date')(key,"yyyy-MM-dd HH:mm:ss")),"count":count});
					}
					
					$("#"+panel_name+panel.id+" .panel_code").attr("title",alt);
					$("#"+panel_name+panel.id+" .panel_code").attr("alt",alt);
					$("#"+panel_name+panel.id+" .panel_code").tooltipster({
						theme: 'tooltipster-shadow',
						contentAsHTML: true,
						interactive: true,
						position:'bottom',
						multiple: true
					});
					
					var obj = $("#"+panel_name+panel.id);
					obj.attr(properties_name+"id",panel.id);
					obj.attr(properties_name+"type",panel.type);
					obj.attr(properties_name+"content",panel.content);
					
					var width = $("#panel_content_"+id_num).width();
					var height = $("#panel_content_"+id_num).height();
					var sparklineCharts = new SparklineCharts();
					sparklineCharts.width = width;
					sparklineCharts.height = height;
					if(content.color == null || $.trim(content.color) == "")
						sparklineCharts.colors = hs_color;
					else
						sparklineCharts.colors = [content.color];
					sparklineCharts.num = id_num;
					sparklineCharts.showX = content.xAxis;
					sparklineCharts.showY = content.yAxis;
					sparklineCharts.legend = content.legend;
					if(content.showTitle)
						sparklineCharts.title = content.title;
					sparklineCharts.xRotate = content.xRotate;
					sparklineCharts.draw("#panel_content_"+id_num,ds);
				});
			}
		}
	}]);
});