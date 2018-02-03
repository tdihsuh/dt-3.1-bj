define(['app'],function(app){
	app.controller('confForwarderCtrl',['$scope','$http','$window','$routeParams','$rootScope',function($scope,$http,$window,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 转发器配置";
		var currentPage = $routeParams.param;
		var pageSize = 5;
		var pageArray = [];
		if(currentPage==undefined){
			currentPage = 1;
		}
		$scope.queryforwarderList = function(currentPage,pageSize){
			var url = "forwarder/list.hs?currentPage="+currentPage+"&pageSize="+pageSize;
			$http.get(url).success(function(data){
				$scope.forwarder =  data.model.forwarderList;
				pageArray.push(data.model.forwarderList.currentPage);
			});
		};
		$scope.queryforwarderList(currentPage,pageSize);
		
		$scope.queryPage = function(currentPage){
			pageArray.push(currentPage);
			$scope.queryforwarderList(currentPage,pageSize);
		};
		$scope.addForwarder = function(){
			if(pageArray[pageArray.length-1]==0){
				location = "#/forwarderAdd/"+1;
			}else{
				location = "#/forwarderAdd/"+pageArray[pageArray.length-1];
			}
		};
		$scope.delForwarder = function(id){
			var currentPage = pageArray[pageArray.length-1];
			if(currentPage==0){
				currentPage = 1;
			}
			var r=confirm("确定删除该条问题？");
			if(r==true){
				var url ="forwarder/del.hs?id="+id+"&currentPage="+currentPage+"&pageSize="+pageSize;
				$http.get(url).success(function(data){
					$scope.forwarder = data.model.forwarderList;
					var flag = data.model.flag;
					var flag1 = data.model.flag1;
					if(flag==true){
						if(flag1==true){
							
						}else{
							$("#confForwardrError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
						}
					}else{
						$("#confForwardrError").html("<b><font color='red'>该条转发器正在使用，请勿删除！！！</font></b>");
					}
					
				});
			}
		};
		$scope.editForwarder = function(forwarderId){
		    if(pageArray[pageArray.length-1]==0){
				location = "#/confForwarderDetail/"+forwarderId+"/"+1;
			}else{
				location = "#/confForwarderDetail/"+forwarderId+"/"+pageArray[pageArray.length-1];
			}
		};
		$scope.loadForwarder = function(id){
			//alert(id);
			var url ="forwarder/loadForwarder.hs?id="+id;
			$window.open(url);
		};	
		
		
		
	}]);
});