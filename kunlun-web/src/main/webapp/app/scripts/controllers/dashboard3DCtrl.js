define(['app','scripts/factories/events','scripts/factories/logs'],function(app,EventItems,LogItems){
	app.controller('dashboard3DCtrl',['$scope','$http','EventItems','LogItems','hs_host','hs_log_index_name',function($scope,$http,EventItems,LogItems,hs_host,hs_log_index_name){
		var eventUrl = hs_host + "/ANOMALY/ANOMALY/_count";
		/**事件计数**/
		$http.get(eventUrl).success(function(data){
			$scope.eventNum = data.count;
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
		/**日志计数**/
		$http.get(hs_host + "/"+hs_log_index_name+"*/_count").success(function(data){
			$scope.logNum = data.count;
		});
		$http.get("app/data/summaryLogs.json").success(function(data){
			var hsChart = new HsCharts();
			hsChart.objId = "logContainer";
			hsChart.data = data;
			hsChart.tooltip_bgColor = "#000000";
			hsChart.tooltip_color = "#FFFFFF";
			
			hsChart.column();
		});
		
		/**健康度**/
		$http.get(hs_host + "/_cat/health?h=status").success(function(data){
			var c=document.getElementById("dashboard_health");
			var cxt=c.getContext("2d");
			cxt.fillStyle = data;
			cxt.beginPath();
			cxt.arc(150,70,70,0,Math.PI*2,true);
			cxt.closePath();
			cxt.fill();
		});
		
		var categories = ['2014-05-01', '2014-05-02', '2014-05-03', '2014-05-04', '2014-05-05'
						,'2014-05-06','2014-05-07','2014-05-08','2014-05-09','2014-05-10','2014-05-11'];
		var columnData = [{
					name: 'invalid-Redirect',
					data: [2489, 2478, 2520, 2252, 2176,2340,0,213,2103,112,2222]
				}, {
					name: 'CSRF',
					data: [201, 131, 381, 125, 112,149,128,112,183,125,111]
				}, {
					name: 'XSS',
					data: [123, 125, 145, 181, 171,156,251,113,172,192,129]
				}, {
					name: 'DDos',
					data: [174, 231, 160, 231, 181,163,331,126,152,127,195]
				}, {
					name: 'Vlunerability',
					data: [262,203,181,105,242,261,257,97,88,81,172]
				}, {
					name: 'mis-ACL',
					data: [263, 201, 183, 100, 240,263,258,98,89,80,110]
				}, {
					name: 'Injection',
					data: [750, 612, 510, 250, 508,198,263,216,210,323,271]
				}, {
					name: 'ms-configuration',
					data: [1480, 1512, 1702, 1201, 1196,1773,1198,1028,1382,1520,1238]
				}]
		var columnChart = new HsCharts();
		columnChart.objId = "eventsColumn";
		columnChart.data = columnData;
		columnChart.xAxis_categories = categories;
		columnChart.xAxis_rotation = -20;
		columnChart.title = "安全态势分析图";
		//columnChart.stackedColumn();
		
		var chart = new Highcharts.Chart({
			chart: {
				renderTo: 'eventsColumn',
				type: 'column',
				marginRight: 35,
				options3d: {
					enabled: true,
					alpha: 0,
					beta: 15,
					viewDistance: 25,
					depth: 40
				}
			},
			title: {
				text: '安全态势分析图'
			},
			plotOptions: {
				column: {
					stacking:'normal'
				}
			},
			xAxis : {
				categories : categories,
				labels:{
					rotation:-35
				}
			},
			yAxis: {
				title: {
					text: ''
				}
			},
			series: columnData,
			exporting: { 
				enabled: false
			},
			credits: {  
				enabled: false
			}
		});
		
		var pieData = [
			['Injection',38],
			['mis-configuration',50],
			['DDos',18],
			['XSS',8],
			['Other',5]
		]
		var pieChart = new HsCharts();
		pieChart.objId = "eventsPie";
		pieChart.data = pieData;
		//pieChart.pie();
		
		
		$('#eventsPie').highcharts({
			chart: {
				type: 'pie',
				options3d: {
					enabled: true,
					alpha: 45,
					beta: 0
				}
			},
			title: {
				text: ''
			},
			tooltip: {
				pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
			},
			plotOptions: {
				pie: {
					allowPointSelect: true,
					cursor: 'pointer',
					depth: 35,
					dataLabels: {
						enabled: true,
						format: '{point.name}'
					}
				}
			},
			series: [{
				type: 'pie',
				name: '所占比例',
				data: pieData
			}],
			exporting: { 
				enabled: false
			},
			credits: {  
				enabled: false
			}
		});
	}])
});