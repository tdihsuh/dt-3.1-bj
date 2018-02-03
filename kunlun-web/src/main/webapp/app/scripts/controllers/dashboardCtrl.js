define(['app','scripts/factories/events','scripts/factories/logs','scripts/factories/common/aliases'],function(app,EventItems,LogItems,Aliases){
	app.controller('dashboardCtrl',['$scope','$http','EventItems','LogItems','hs_es','hs_info','hs_auto_refresh','hs_map_url','$filter','$timeout','hs_color','$rootScope','$location','$route','Aliases',
	                                function($scope,$http,EventItems,LogItems,hs_es,hs_info,hs_auto_refresh,hs_map_url,$filter,$timeout,hs_color,$rootScope,$location,$route,Aliases){
		$rootScope.app_nav = "首页 / 仪表盘";
		$("#cmbbody").css("overflow","auto");
		var eventUrl = hs_es.host_name + "/" +hs_es.log_index_name+ "*/"+hs_es.anormal_type_name+"/_count?ignore_unavailable=true";
		
		$rootScope.global = {
		    "systemTimeRange":{
		    	"id":null,
			    "timeValue":null,
			    "timeUnit":null,
			    "timeRefresh":null
		    }
		}
		$scope.dashboard = {
				"timeUnits" : [
					{"key":"second","val":"秒钟之前"},
					{"key":"minute","val":"分钟之前"},
					{"key":"hour","val":"小时之前"},
					{"key":"day","val":"天之前"}
				],
				"timeUnitSelected":"",
				"indices":""
		}
		
		$http.post("systemTimeRange/queryByCategory.hs?category=dashboard").success(function(data){
			var val = data.model.systemTimeRange;
			$rootScope.global.systemTimeRange.id = val.id;
			$rootScope.global.systemTimeRange.timeValue = val.timeValue;
			$rootScope.global.systemTimeRange.timeUnit = val.timeUnit;
			$rootScope.global.systemTimeRange.timeRefresh = val.timeRefresh;
			for(var i in $scope.dashboard.timeUnits){
				if($scope.dashboard.timeUnits[i].key == val.timeUnit)
					$scope.dashboard.timeUnitSelected = $scope.dashboard.timeUnits[i];
			}
			var db_now = new Date();
			var db_from = hsTimes.getBeforeDateBySpecify(val.timeUnit,val.timeValue,db_now);
			/**加载地图**/
			var hsMap = new HsMap();
			hsMap.label = "map";
			hsMap.mapurl = hs_map_url;
			hsMap.init();
			Aliases.getIndices($scope.expand_range(db_from,db_now,"day")).then(function(indices){
				$scope.dashboard.indices = indices;
				$rootScope.panel = {"indices":indices}
				$scope.dashboard.panelUrl = 'app/template/panel/show.html';
				
				if(indices == null || indices == ""){
					$scope.logNum = 0;
					$scope.eventnum = 0;
					return;
				}
				
				var eventUrl = hs_es.host_name + "/" + indices + "/" + hs_es.anormal_type_name + "/_count?ignore_unavailable=true";
				var logUrl = hs_es.host_name + "/"+indices + "/_count?ignore_unavailable=true";
				$scope.logCount(logUrl,eventUrl);
				$scope.loadMap(hsMap,indices);
				
				var dsTimer; 
				$scope.timeouts = function(){
					dsTimer = $timeout(function(){
						$scope.logCount(logUrl,eventUrl);
						$scope.loadMap(hsMap,indices);
						$scope.timeouts();
					},$rootScope.global.systemTimeRange.timeRefresh*1000);
				};
				$scope.$on('$destroy', function() {
					$timeout.cancel(dsTimer);
				});
				$scope.timeouts();
			});
		});
		
		$scope.saveTimeRange = function(){
			$rootScope.global.systemTimeRange.timeUnit = $scope.dashboard.timeUnitSelected.key;
			var systemTimeRange = $rootScope.global.systemTimeRange;
			$http.post("systemTimeRange/save.hs",systemTimeRange).success(function(data){
				$route.reload();
			});
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
		}
		
		$http.get("app/data/summaryEvents.json").success(function(data){
			var hsChart = new HsCharts();
			hsChart.objId = "eventContainer";
			hsChart.data = data;
			hsChart.tooltip_bgColor = "#000000";
			hsChart.tooltip_color = "#FFFFFF";
			hsChart.series_color = "#e55955";
			
			hsChart.column();
		});
		/**日志计数**/
		$scope.logCount = function(logUrl,eventUrl){
			/**事件计数**/
			$http.get(eventUrl).success(function(data){
				$scope.eventNum = data.count;
				
				$http.get(logUrl).success(function(d){
					$scope.logNum = d.count - $scope.eventNum;
				});
			});
		};
		$http.get("app/data/summaryLogs.json").success(function(data){
			var hsChart = new HsCharts();
			hsChart.objId = "logContainer";
			hsChart.data = data;
			hsChart.tooltip_bgColor = "#000000";
			hsChart.tooltip_color = "#FFFFFF";
			hsChart.series_color = "#6c6c6c";
			hsChart.column();
		});
		
		/**健康度**/
		$http.get(hs_es.host_name + "/_cat/health?h=status").success(function(data){
			var c=document.getElementById("dashboard_health");
			var cxt=c.getContext("2d");
			cxt.fillStyle = data;
			cxt.beginPath();
			cxt.arc(150,70,70,0,Math.PI*2,true);
			cxt.closePath();
			cxt.fill();
		});
		
		/**集群状态**/
		$scope.per_cpu = 0;
		$scope.per_mem = 0;
		$scope.per_disk = 0;
		$scope.per_load = 0;
		$scope.per_load_5 = 0;
		$scope.per_load_15 = 0;
		
		function showOsInfo(){
			$http.get(hs_es.host_name + "/_nodes/stats/os").success(function(data){
				var nodes = data.nodes;
				var cpu_free = 0;
				var mem_per = 0;
				var n = 0;
				var per_load_1 = 0;
				var per_load_5 = 0;
				var per_load_15 = 0;
				for(var index in nodes){
					n ++;
					cpu_free += nodes[index].os.cpu.idle;
					mem_per += nodes[index].os.mem.used_percent;
					per_load_1 += nodes[index].os.load_average[0];
					per_load_5 += nodes[index].os.load_average[1];
					per_load_15 += nodes[index].os.load_average[2];
				}
				$scope.per_cpu = $filter("number")(100-cpu_free/n,0);
				$scope.per_mem = $filter("number")(mem_per/n,0);
				$scope.per_load = $filter("number")(per_load_1/n*100,0);
				$scope.per_load_5 = $filter("number")(per_load_5/n,2);
				$scope.per_load_15 = $filter("number")(per_load_15/n,2);
			});
		};
		
		var timerId;
		$scope.timeout = function(){
			timerId = $timeout(function(){
				showOsInfo();
				$scope.timeout();
			},hs_auto_refresh);
		};
		$scope.$on('$destroy', function() {
			$timeout.cancel(timerId);
		});
		$scope.timeout();
		showOsInfo();
		
		$http.get(hs_es.host_name + "/_nodes/stats/fs").success(function(data){
			var disk_total = 0;
			var disk_free = 0;
			var nodes = data.nodes;
			for(var index in nodes){
				disk_total += nodes[index].fs.total.total_in_bytes;
				disk_free += nodes[index].fs.total.available_in_bytes;
			}
			$scope.per_disk = $filter("number")((disk_total-disk_free)/disk_total*100,0);
		});
		
		$scope.per_load_opt = {animate:false,barColor:'#c77f54',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_disk_opt = {animate:false,barColor:'#f18d2c',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_cpu_opt = {animate:false,barColor:'#ca607e',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_mem_opt = {animate:false,barColor:'#d8ad45',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		
		var categories = ['2014-05-01', '2014-05-02', '2014-05-03', '2014-05-04', '2014-05-05'
						,'2014-05-06','2014-05-07','2014-05-08','2014-05-09','2014-05-10','2014-05-11','2014-05-12','2014-05-13','2014-05-14','2014-05-15'];
		var columnData = [{
					name: 'XSS',
					data: [1231, 1251, 1451, 1811, 1711,1563,2512,1132,1721,1924,1291,1238,1329,1023,1122]
				}, {
					name: 'DDos',
					data: [742, 312, 602, 312, 812,631,331,261,523,275,952,872,523,271,382]
				}, {
					name: 'Injection',
					data: [750, 612, 510, 250, 508,198,263,216,210,323,271,353,462,215,532]
				}, {
					name: 'ms-configuration',
					data: [1480, 1512, 1702, 1201, 1196,1773,1198,1028,1382,1520,1238,1320,1230,1333,1222]
				}]
		var columnChart = new HsCharts();
		columnChart.objId = "eventsColumn";
		columnChart.data = columnData;
		columnChart.xAxis_categories = categories;
		columnChart.xAxis_rotation = -20;
		columnChart.title = "安全态势分析图";
		columnChart.stackedColumn();
		
		$scope.loadMap = function(hsMap,indices){
			var temp_host = hs_es.host_name + "/"+indices+ "/"+hs_es.anormal_type_name+"/_search?ignore_unavailable=true";
			
			/**构建攻击源对象**/
			var para = {
				"aggs":{
					"cities":{
						"terms":{"field":"city","size":0},
						"aggs":{
							"ips":{
								"terms":{"field":"c_ip","size":5}
							}
						}
					}
				},
				"size":0
			};
			$http.post(temp_host,para).success(function(data){					
				var datas = data.aggregations.cities.buckets;
				var ips;
				var tObj;
				for(var i=0;i<datas.length;i++){
					hsMap.sourceData[i] = {};
					hsMap.sourceData[i]["city"] = datas[i].key;
					hsMap.sourceData[i]["attacksNum"] = datas[i].doc_count;
					
					hsMap.sourceData[i]["ips"] = [];
					ips = datas[i].ips.buckets;
					for(var j=0;j<ips.length;j++){
						tObj = {};
						tObj[ips[j].key_as_string] = ips[j].doc_count;
						hsMap.sourceData[i]["ips"].push(tObj);
					}
				}
				/***每个城市只取一个经纬度***/
				var par = {
					"aggs":{
						"cities":{
							"terms":{"field":"city","size":0},
							"aggs":{
								"geos":{
									"terms":{"field":"geo","size":1}
								}
							}
						}
					},
					"size":0
				};
				$http.post(temp_host,par).success(function(data){
					var datas = data.aggregations.cities.buckets;
					var locations;
					for(var i=0;i<hsMap.sourceData.length;i++){
						for(var j=0;j<datas.length;j++){
							if(datas[j].key == hsMap.sourceData[i].city){
								var geos = datas[j].geos.buckets;
								if(geos[0]){
									var eles = geos[0].key.split(",");
									hsMap.sourceData[i]["longitude"] = eles[1];
									hsMap.sourceData[i]["latitude"] = eles[0];
								}
								break;
							}
						}
					}
					
					hsMap.sourceMap();
				});
			});
		};
		$(function(){
		});
	}])
});