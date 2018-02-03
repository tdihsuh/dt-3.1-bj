define(['app'],function(app){
	app.controller('confDatasourceErrorCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 数据源配置";	
	}]);
});