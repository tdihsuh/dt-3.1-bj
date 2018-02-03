define(['app'],function(app){
	app.controller("userCtrl",["$scope","$http",'User',function($scope,$http,User){
		var user = new User();
		user.query().then(function(u){
			$scope.user = u;
		});
	}])
});