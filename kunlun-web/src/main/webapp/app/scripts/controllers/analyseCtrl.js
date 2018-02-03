define(['app','scripts/factories/events','scripts/factories/logs','scripts/factories/pieCharts','scripts/factories/common/aliases','scripts/factories/charts/stackedBarCharts'],function(app,EventItems,LogItems,PieCharts,Aliases,StackedBarCharts){
	app.controller('analyseCtrl',['$scope','$http','$filter','EventItems','LogItems','hs_es','hs_color','PieCharts','$rootScope','Aliases','$route','StackedBarCharts',
	                              function($scope,$http,$filter,EventItems,LogItems,hs_es,hs_color,PieCharts,$rootScope,Aliases,$route,StackedBarCharts){
		$rootScope.app_nav = "首页 / 智能分析";
		$("#cmbbody").css("overflow","auto");
		$scope.analyse = {
				"timeUnits" : [
					{"key":"second","val":"秒钟之前"},
					{"key":"minute","val":"分钟之前"},
					{"key":"hour","val":"小时之前"},
					{"key":"day","val":"天之前"}
				],
				"timeUnitSelected":"",
				"indices":"",
				"systemTimeRange":{
			    	"id":null,
				    "timeValue":null,
				    "timeUnit":null,
				    "timeRefresh":null
			    },
				"log":{
					"pages":null,
					"fieldName":null,
					"fieldValue":null,
					"indices":null
				},
				"logModule":""	//日志显示方式："detail":日志详情	"status":请求状态统计
		}
		
		$http.post("systemTimeRange/queryByCategory.hs?category=analyse").success(function(data){
			var val = data.model.systemTimeRange;
			$scope.analyse.systemTimeRange.id = val.id;
			$scope.analyse.systemTimeRange.timeValue = val.timeValue;
			$scope.analyse.systemTimeRange.timeUnit = val.timeUnit;
			$scope.analyse.systemTimeRange.timeRefresh = val.timeRefresh;
			for(var i in $scope.analyse.timeUnits){
				if($scope.analyse.timeUnits[i].key == val.timeUnit)
					$scope.analyse.timeUnitSelected = $scope.analyse.timeUnits[i];
			}
			var db_now = new Date();
			var db_from = hsTimes.getBeforeDateBySpecify(val.timeUnit,val.timeValue,db_now);
			
			Aliases.getIndices($scope.expand_range(db_from,db_now,"day")).then(function(indices){
				$scope.analyse.indices = indices;
				$scope.showEventType(indices);
				$scope.showEvents(indices);
//				$scope.aggsUrl(indices);
			});
		});
		
		$scope.showEventType = function(indices){
			/**威胁类别**/
			var url = hs_es.host_name + "/" +indices+ "/" + hs_es.anormal_type_name + "/_search?ignore_unavailable=true";
			var param = {
				"aggs":{
					"category":{
						"terms":{
							"field":"category.raw"
						}
					}
				},
				"size":0
			}
			$http.post(url,param).success(function(data){
				var categorys = data.aggregations.category.buckets;
				var ets = new Array();
				if(categorys){
					for(var i in categorys){
						ets[i] = {"name":categorys[i].key,"num":categorys[i].doc_count,"nameShow":""};
						if(categorys[i].key=="MisConfiguration"){
							ets[i].nameShow="状态异常";
						}else if(categorys[i].key=="SQL Injection"){
							ets[i].nameShow="请求参数异常";
						}else{
							ets[i].nameShow=categorys[i].key;
						}
					}
					var pieCharts = new PieCharts();
					pieCharts.width = $("#securityContainer").width();
					pieCharts.height = $("#securityContainer").height();
					pieCharts.colors = hs_color;
					pieCharts.val = "doc_count";
					pieCharts.draw("#securityContainer",categorys);
				}
				$scope.categorys = ets;
			});
		};
		
		/**显示事件**/
		$scope.showEvents = function(indices,category,currentPage){
			if(category)
				category = category.toLowerCase();
			$scope.selectedRow = null;
			var para = {};
			para["sort"] = {"@timestamp":{order:"desc"}};
			if(category){
				para["query"] = {match:{category:category}}
				//if(category == "misconfiguration"){
					para["sort"] = {"counter":{order:"asc"}};
				//}
			}
			EventItems.query(hs_es.host_name+"/"+ indices +"/"+hs_es.anormal_type_name+"/_search?ignore_unavailable=true",para,currentPage).then(function(data){
				$scope.events = data;
				
				var eventPageNum = data.pageNum;
				var pages = [];
				
				var n = 0;
				var curr = data.currentPage;
				for(var i=curr-5;i<curr-1;i++){
					if(i >= 0)
						pages[n++] = i+1;
				}
				pages[n++] = curr;
				for(var i=curr+1;i<curr+5 && i <= eventPageNum;i++){
					pages[n++] = i;
				}
				
				$scope.eventPages = pages;
				$scope.category = category;
			});
			/**每次加载事件列表时清空之前显示的日志列表**/
			$scope.nodes = null;
			$scope.eventPages = [];
		};
		/**显示日志**/
		$scope.showLogs = function(currentPage){
			
			var logurls = hs_es.host_name + "/" +$scope.analyse.log.indices+ "/_search?ignore_unavailable=true";
			
			var para = {
				sort:[
					{
						"@timestamp":{order:"desc"}
					}
				],
				query:{
					bool:{
						must:[{term:{}}],
						must_not:{
							term:{_type:"anomaly"}
						}
					}
				}
			}
			if($scope.analyse.log.fieldName=="cs_uri_stem.raw"){
				para.query.bool.must[0].term["sc_status"] = 404;
				para.query.bool.must[1]={term:{}};
				para.query.bool.must[1].term[$scope.analyse.log.fieldName] = $scope.analyse.log.fieldValue;
			}else{
				para.query.bool.must[0].term[$scope.analyse.log.fieldName] = $scope.analyse.log.fieldValue;
			}
			
			
			LogItems.query(logurls,para,currentPage).then(function(data){
				$scope.logs = data;
				
				var logPageNum = data.pageNum;
				var pages = [];
				
				var n = 0;
				var curr = data.currentPage;
				for(var i=curr-5;i<curr-1;i++){
					if(i >= 0)
						pages[n++] = i+1;
				}
				pages[n++] = curr;
				for(var i=curr+1;i<curr+5 && i <= logPageNum;i++){
					pages[n++] = i;
				}
				$scope.analyse.log.pages = pages;
			});
		}
		$scope.showDetailFields1 = function(id){
			var b = false;
			if($("#log_"+id).css("display") == "none")
				b = false;
			else
				b = true;
			$(".detail_field").css("display","none");
			if(b)
				$("#log_"+id).css("display","none");
			else
				$("#log_"+id).css("display","block");
		}
		
		$scope.showLogDetail = function(row,obj){
			$scope.selectedRow = row;
			
			if(!$scope.analyse.logModule){
				$scope.analyse.logModule = "detail";
			}
			
			var indexs = obj._source.indices;
			var indices = ""
			for(var i=0;i<indexs.length;i++){
				if(i > 0){
					indices += ",";
				}
				indices += indexs[i];
			}
			$scope.analyse.log.indices = indices;
			
			if(obj._source.eventType == "sc404"){
				$scope.analyse.log.fieldName = "cs_uri_stem.raw";
				$scope.analyse.log.fieldValue = obj._source.url;
			}else{
				$scope.analyse.log.fieldName = "cs_uri_query.raw";
				$scope.analyse.log.fieldValue = obj._source.url_query;
			}
			$scope.showLogs();
//			$scope.showLogStatus();
		}
		
		$scope.showLogStatus = function(){
			var logurls = hs_es.host_name + "/" +$scope.analyse.log.indices+ "/_search?ignore_unavailable=true";
			var para = {
				"query":{
					"term":{}
				},
				 "aggs":{
					"times":{
					      "date_histogram":{
					        "field":"@timestamp",
					        "interval":"hour"
					      },"aggs":{
					          "status":{
					            "terms":{
					              "field":"sc_status"
					            }
					          }
					      }
					}
				 },"size":0
			};
			para.query.term[$scope.analyse.log.fieldName] = $scope.analyse.log.fieldValue;
			$http.post(logurls,para).success(function(data){
				var datas = data.aggregations.times.buckets;
				var arr = [];
				var key;
				var val;
				for(var i in datas){
					var temp = {};
					temp["key"] = $filter('date')(datas[i].key_as_string,"yyyy-MM-dd HH:mm:ss");
					var d = datas[i].status.buckets;
					for(var s in d){
						key = d[s].key;
						val = d[s].doc_count;
						temp[key] = val;
					}
					arr.push(temp);
				}
				var stackedBarCharts = new StackedBarCharts();
				stackedBarCharts.width = $("#log_status").parent().parent().width();
				stackedBarCharts.height = $("#log_status").height();
				stackedBarCharts.colors = hs_color;
				stackedBarCharts.num = 10;
				stackedBarCharts.showX = true;
				stackedBarCharts.showY = true;
				stackedBarCharts.legend = true;
				stackedBarCharts.xRotate = 30;
				stackedBarCharts.draw("#log_status",arr);
			});
		}
		
		$http.get("app/data/summaryLogs.json").success(function(data){
			var hsChart = new HsCharts();
			hsChart.objId = "logContainer";
			hsChart.data = data;
			hsChart.tooltip_bgColor = "#000000";
			hsChart.tooltip_color = "#FFFFFF";
			
			hsChart.column();
		});
		$http.get("app/data/summaryEvents.json").success(function(data){
			var hsChart = new HsCharts();
			hsChart.objId = "eventContainer";
			hsChart.data = data;
			hsChart.tooltip_bgColor = "#000000";
			hsChart.tooltip_color = "#FFFFFF";
			hsChart.series_color = "#6E587A";
			
			hsChart.column();
		});
		$scope.falseAlarm = function(url,category,currentPage,indices){
			if(confirm("确定要将地址： '"+url+"' 标记为误报？")){
			/**删除anomaly表中相同的url记录**/
			$http.delete(hs_es.host_name+"/"+hs_es.event_index_name+"*/"+hs_es.anormal_type_name+"/_query?q=url:\""+url+"\"").success(function(data){
				/**之前操作为更新建模表中type类型为status_404的url记录且malicious为false**/
				/*目前改为
				 * 1.通过status_404的url搜索建模表
				 * 2.删除建模表中的数据
				 * 3.手工表中加入该条数据
				 * */	
				$http.post(hs_es.host_name+"/"+hs_es.model_index_name+"/"+hs_es.model_type_404+"/_search?q=url:\""+url+"\"").success(function(d){
						console.log(d.hits.hits[0]);
						if(d.hits.hits[0]){
						var id = d.hits.hits[0]._id;
						var url= d.hits.hits[0]._source.url;
						var hashId=$scope.hashCode(url);
						var para = {"_id":hashId,"url":url};
						$http.delete(hs_es.host_name+"/"+hs_es.model_index_name+"*/"+hs_es.model_type_404+"/_query?q=url:\""+url+"\"").success(function(){
							$http.post(hs_es.host_name+"/"+hs_es.manual_model_name+"/"+hs_es.model_type_404+"/"+hashId+"/_create",para).success(function(){
								$scope.showEvents(indices,category,currentPage);
							});
						});
						/**var para = {"script":"ctx._source.malicious = false"};
						$http.post(hs_es.host_name+"/"+hs_es.model_index_name+"/"+hs_es.model_type_404+"/"+id+"/_update",para).success(function(){
							$scope.showEvents(indices,category,currentPage);
						});**/
						}else{
							alert("未找到匹配的模型数据！");
						}
				});
			});		
			}
		};
		$scope.isNull=function(str){
			return str == null || str.value == "";
		};
		$scope.hashCode=function(strKey){
			var hash = 0;  
	        if(!$scope.isNull(strKey))  
	        {  
	            for (var i = 0; i < strKey.length; i++)  
	            {  
	                hash = hash * 31 + strKey.charCodeAt(i);  
	                hash = $scope.intValue(hash);  
	            }  
	        }  
	        return hash; 
		};
		$scope.intValue=function(num){
			var MAX_VALUE = 0x7fffffff;  
	        var MIN_VALUE = -0x80000000;  
	        if(num > MAX_VALUE || num < MIN_VALUE)  
	        {  
	            return num &= 0xFFFFFFFF;  
	        }  
	        return num;  
		};
		$scope.expand_range = function(starts, end, interval){
			var range;
	        var start = moment(starts).clone();
	        range = [];
	        while (start.isBefore(end)) {
	          range.push(start.clone());
	          switch (interval) {
	          case 'hour':
	            start.add(1,'hours');
	            break;
	          case 'day':
	        	start.add(1,'days');
	            break;
	          case 'week':
	        	start.add(1,'weeks');
	            break;
	          case 'month':
	        	start.add(1,'months');
	            break;
	          case 'year':
	        	start.add(1,'years');
	            break;
	          }
	        }
	        range.push(moment(end).clone());
	        return range;
		};
		$scope.saveTimeRange = function(){
			$scope.analyse.systemTimeRange.timeUnit = $scope.analyse.timeUnitSelected.key;
			var systemTimeRange = $scope.analyse.systemTimeRange;
			$http.post("systemTimeRange/save.hs",systemTimeRange).success(function(data){
				$route.reload();
			});
		};
		$scope.aggsUrl = function(indices){
			if(indices == null || indices == "") return;
			var logurls = hs_es.host_name + "/" +indices+ "/anomaly/_search?ignore_unavailable=true";
			var para = {
				"query":{
					"term":{
						"eventType":"sc404"
					}
				},
				"aggs":{
					"urls":{
						"terms":{
							"field":"url.raw"
						},"aggs":{
							"ips":{
								"terms":{
									"field":"c_ip"
									,"size":0
								}
							}
						}
					}
				},"size":0
			};
			$http.post(logurls,para).success(function(data){
				$scope.aggsUrls = data.aggregations.urls.buckets;
			});
		}
	}])
});