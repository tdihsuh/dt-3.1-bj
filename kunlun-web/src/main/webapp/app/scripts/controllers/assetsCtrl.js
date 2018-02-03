define(['app'],function(app){
	app.controller('assetsCtrl',['$scope','$http',function($scope,$http){
		$http.get("app/data/assets.json").success(function(data){
			$scope.assets = data;
		});
	}])
});