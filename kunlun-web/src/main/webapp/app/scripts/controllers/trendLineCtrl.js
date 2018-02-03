define(['app','d3','../charts/trendLineCharts'],function(app,d3,trendLineCharts){
	app.controller("trendLineCtrl",["$scope", "$http", 'hs_es','hs_auto_refresh', '$timeout','$filter','$rootScope', function($scope, $http, hs_es,hs_auto_refresh, $timeout,$filter,$rootScope){
		$rootScope.app_nav = "首页 / 报表管理 / 走势图";
		var url = hs_es.host_name+"/"+hs_es.log_index_name+"*/_search";
		var currentTime = new Date().toUTCString();//系统当前时间
		//currentTime = hsTimes.getBeforeDateTimeByMinute(0,currentTime);
		currentTime = new Date("2013-12-16T00:02:00.000Z");
		var fromTime = hsTimes.getBeforeDateTimeBySecond(30,currentTime);//30分钟前
		var param = {
			"query":{
				"bool":{
					"must":[{
						"range":{
							"datetime":{
								"from" : fromTime,
								"to" : currentTime
							}
						}
					},{
						"term":{"sc_status":"404"}
					}]
				}
			},
			"aggs":{
				"group_by_sid":{
					"terms":{
						"field":"c_ip",
						"size":0
					}
				}
			},
			"size":0
		};
		function showCharts(){
			param.query.bool.must[0].range.datetime.from = fromTime;
			param.query.bool.must[0].range.datetime.to = currentTime;
			$http.post(url,param).success(function(data){
				var obj = data.aggregations.group_by_sid.buckets;
				
				var url_404 = [];
				for(var ele in obj){
					var str = {};
					str["sid"] = obj[ele].key_as_string;
					str["404"] = obj[ele].doc_count;
					str["url"] = 0;
					url_404.push(str);
				}
				var para = {
					"query":{},
					"aggs":{},
					"size":0
				};
				para.query["range"] = param.query.bool.must[0].range;
				para.aggs = param.aggs;
				$http.post(url,para).success(function(data){
					var obj = data.aggregations.group_by_sid.buckets;
					for(var i=0;i<url_404.length;i++){
						for(var ele in obj){
							if(obj[ele].key_as_string == url_404[i].sid){
								url_404[i].url = obj[ele].doc_count;
								break;
							}
						}
					}
					trendLineCharts("#trendLineChart",url_404);
				});
			});
		}
		showCharts();
		var timerId;
		var per = hs_auto_refresh/1000;
		$scope.timeout = function(){
			timerId = $timeout(function(){
				currentTime = hsTimes.getBeforeDateTimeBySecond(-1*per,currentTime);
				fromTime = hsTimes.getBeforeDateTimeBySecond(30,currentTime);//30分钟前
				showCharts();
				$scope.timeout();
			},hs_auto_refresh);
		};
		$scope.$on('$destroy', function() {
			$timeout.cancel(timerId);
		});
		$scope.timeout();
	}]);
});