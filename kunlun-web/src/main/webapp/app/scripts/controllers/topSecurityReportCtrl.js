define(['app','scripts/factories/logs','d3'],function(app,LogItems,d3){
	app.controller('topSecurityReportCtrl',['$scope','$http','$filter','$sce','LogItems','hs_es','hs_color','hs_info','$routeParams','$rootScope',function($scope,$http,$filter,$sce,LogItems,hs_es,hs_color,hs_info,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 报表管理 / TOP5安全威胁事件";
		if(!$scope.report){
			$scope.report = {};
		};
		
		/**设置时间选择框**/
		$(function(){
			$("#datepicker_from").datepicker($.datepicker.regional["zh"]).datepicker("option","dateFormat","yy-mm-dd");
			$("#datepicker_to").datepicker($.datepicker.regional["zh"]).datepicker("option","dateFormat","yy-mm-dd");
			$('#datepicker_from').datepicker('setDate', -7 );
			$('#datepicker_to').datepicker('setDate', new Date()) ;
			$('#timeInterval').get(0).selectedIndex = 1;
		});
		
		$scope.checkDate = function(){
			var starttime=$("#datepicker_from").datepicker('getDate');
			alert("start:"+starttime);
		};

		
		/**默认取近一周数据**/
		
		$scope.query = function(currentPage){
			$scope.report.logNum = 0;
			if(!$scope.report.datetime_to) $scope.report.datetime_to = new Date();
			var days = hsTimes.getDaysBetweenDate($scope.report.datetime_from,$scope.report.datetime_to);
			if(days.length > 0){
				var days_ = "";
				for(var i=0;i<days.length;i++){
					if(i > 0) days_ += ",";
					days_ += hs_es.log_index_name+days[i].format("yyyyMMdd");
				}
				$http.get(hs_es.host_name + "/" + days_  + "/_aliases?ignore_missing=true").success(function(data1){
					var n = 0;
					var valid_days = "";
					for(var o in data1){
						if(n > 0)
							valid_days += ",";
						valid_days += o;
						n ++;
					}

					if(n > 0){
						$scope.report.url = hs_es.host_name + "/" + valid_days;
						//$scope.queryFields();
						//$scope.queryLogs(currentPage);
					}else{
						$scope.report.url = null;
						$scope.report.fields = [];
						//generateSearchSummary();
						$scope.highlight = null;
						$scope.logs = null;
						$("#reportContainer").empty();
					}
				});
			}
		};

		
		
		$scope.showReport = function(){
			$('#reportContainer').highcharts({
				chart: {
					type: 'column'
				},
				title: {
					text: ''
				},
				subtitle: {
					text: ''
				},
				xAxis: {
					categories: [
						'Injection',
						'mis-configuration',
						'DDoS',
						'XSS',
						'CSRF'
					],
					labels: {
						rotation: -45,
						align: 'right',
						style: {
							fontSize: '13px',
							fontFamily: 'Verdana, sans-serif'
						}
					}
				},
				yAxis: {
					min: 0,
					title: {
						text: ''
					}
				},
				tooltip: {
					headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
					pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
						'<td style="padding:0"><b>{point.y} </b></td></tr>',
					footerFormat: '</table>',
					shared: true,
					useHTML: true
				},
				plotOptions: {
					column: {
						pointPadding: 0.2,
						borderWidth: 0
					}
				},
				series: [{
					name: '上周',
					data: [1900, 1250, 1021, 613, 252]
		
				}, {
					name: '本周',
					data: [1190,1921,732,725,203]
		
				}],
				credits: {  
					text: ''
				}
			});
		};
		
		$scope.showReport();
	
		$scope.exportReport = function(){
			window.open('images/report/topSecurity.pdf');
		};
	}]);
});