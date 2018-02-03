define(['app'],function(app){
	app.controller('multiSearchCtrl',['$scope','User','$rootScope',
	                             function($scope,User,$rootScope){
		$rootScope.app_nav = "首页 / 多元搜索";
		$("#cmbbody").css("overflow","hidden");
		$scope.user = {
			"isManager":false,	//是否有管理权限
			"iframeSrc":null      //iframe地址
		};
		var user = new User();
			user.query().then(function(u){
				for(var i in u.roles){
					if(u.roles[i].name == "ROLE_ADMIN"){
						$scope.user.isManager = true;
					}
				}
			});
	}]);
});