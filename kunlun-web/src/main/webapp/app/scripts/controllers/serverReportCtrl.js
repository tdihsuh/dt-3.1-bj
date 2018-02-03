define(['app'],function(app){
	app.controller('serverReportCtrl',['$scope','$http','$rootScope',function($scope,$http,$rootScope){
		$rootScope.app_nav = "首页 / 报表管理 / 服务器";
		$scope.showReport = function(){
			$('#container').highcharts({
				chart: {
					type: 'column'
				},
				title: {
					text: ''
				},
				xAxis: {
					categories: ['W3SVC1', 'W3SVC2', 'W3SVC3', 'W3SVC4', 'W3SVC5','W3SVC6','W3SVC7','W3SVC8','W3SVC9','W3SVC10']
				},
				yAxis: {
					min: 0,
					title: {
						text: ''
					},
					stackLabels: {
						enabled: true,
						style: {
							fontWeight: 'bold',
							color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
						}
					}
				},
				legend: {
					layout: 'vertical',
					align: 'right',
					x: 20,
					verticalAlign: 'top',
					y: 30,
					floating: true,
					backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || 'white',
					borderColor: '#CCC',
					borderWidth: 1,
					shadow: true
				},
				tooltip: {
					formatter: function() {
						return '<b>'+ this.x +'</b><br/>'+
							this.series.name +': '+ this.y;
					}
				},
				plotOptions: {
					column: {
						stacking: 'normal'
					}
				},
				series: [{
					name: 'bad-cookie',
					data: [10, 200, 223, 132, 120,8,183,238,108,226]
				}, {
					name: 'invalid-Redirect',
					data: [489, 478, 200, 252, 240,240,240,240,10,228]
				}, {
					name: 'Vlunerability',
					data: [262,203,181,105,242,261,257,97,88,81]
				}, {
					name: 'mis-ACL',
					data: [263, 201, 183, 100, 240,263,258,98,89,80]
				}, {
					name: 'SesitiveData',
					data: [123, 248, 489, 243, 221,198,125,132,123,120]
				}, {
					name: 'CSRF',
					data: [200, 130, 119, 252, 220,198,182,120,260,18]
				}, {
					name: 'XSS',
					data: [230, 250, 210, 189, 160,162,130,98,100,20]
				}, {
					name: 'DDos',
					data: [749, 238, 303, 238, 243,230,98,89,200,78]
				}, {
					name: 'ms-configuration',
					data: [480, 512, 302, 201, 96,173,98,246,173,103]
				}, {
					name: 'Injection',
					data: [750, 612, 510, 250, 508,198,263,216,210,323]
				}],
				credits: {  
					text: ''
				}
			});
		};
		$scope.showReport();
		$scope.exportReport = function(){
			window.open('images/report/server.pdf');
		};
	}])
});