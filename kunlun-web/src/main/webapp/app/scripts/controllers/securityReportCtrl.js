define(['app'],function(app){
	app.controller('securityReportCtrl',['$scope','$http','$rootScope',function($scope,$http,$rootScope){
		
		$rootScope.app_nav = "首页 / 报表管理 / 安全威胁事件";
	
		$scope.showReport = function(){
			$('#container').highcharts({
				chart: {
					plotBackgroundColor: null,
					plotBorderWidth: null,
					plotShadow: false
				},
				title: {
					text: '事件类型【2014/4/8~2014/5/7】'
				},
				tooltip: {
					pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
				},
				plotOptions: {
					pie: {
						allowPointSelect: true,
						cursor: 'pointer',
						dataLabels: {
							enabled: true,
							format: '<b>{point.name}</b>: {point.percentage:.1f} %',
							style: {
								color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
							}
						}
					}
				},
				series: [{
					type: 'pie',
					name: '所占比例',
					data: [
						['Injection',38],
						['mis-configuration',26],
						['DDos',18],
						['XSS',8],
						['CSRF',3],
						['Sensitive',2],
						['Other',5]
					]
				}],
				credits: {  
					text: ''
				}
			});
		};
		
		$scope.showReport();
		
		$scope.exportReport = function(){
			window.open('images/report/security.pdf');
		};
	}])
});