define(['app'],function(app){
	app.controller('complexReportCtrl',['$scope','$http','$rootScope',function($scope,$http,$rootScope){
		$rootScope.app_nav = "首页 / 报表管理 / 报表综合导出";
		$scope.exportReport = function(){
			window.open('images/report/complex.pdf');
		};
	}])
});