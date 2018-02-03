define(['app'],function(app){
	app.controller('confDataSourceCtrl',['$scope','$http','$routeParams','$sce','$rootScope',function($scope,$http,$routeParams,$sce,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 数据源配置";
		$("#cmbbody").css("overflow","auto");
		var currentPage = $routeParams.param;
		$scope.showLineNum = null;
		var pageSize = 5;
		var pageArray = [];
		if(currentPage==undefined){
			currentPage = 1;
		}
		$scope.queryList = function(currentPage,pageSize){
			var urlList = "datasource/list.hs?currentPage="+currentPage+"&pageSize="+pageSize;
			$http.get(urlList).success(function(data){
				   $scope.datasource =data.model.datasource;
				   pageArray.push(data.model.datasource.currentPage);
			});
		};
		$scope.queryList(currentPage,pageSize);
		
		$scope.queryPage = function(currentPage){
			pageArray.push(currentPage);
			$scope.queryList(currentPage,pageSize);
		};
		
		$scope.addDatasource = function(){
			if(pageArray[pageArray.length-1]==0){
				location = "#/datasourceAdd/"+1;
			}else{
				location = "#/datasourceAdd/"+pageArray[pageArray.length-1];
			}
		};
		$scope.delDatasource = function(id,$event){
			var currentPage = pageArray[pageArray.length-1];
			if(currentPage==0){
				currentPage = 1;
			}
			$event.stopPropagation();
			var r=confirm("确定删除该条问题？");
			if(r==true){
				var url ="datasource/del.hs?id="+id+"&currentPage="+currentPage+"&pageSize="+pageSize;
				$http.get(url).success(function(data){
					$scope.datasource = data.model.datasource;
					var flag = data.model.flag;
					if(flag==true){
						
					}else{
						$("#datasourceError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
					}
				});
			}
		};
		$scope.editDatasource = function(datasourceId,$event){
			$event.stopPropagation();
			if(pageArray[pageArray.length-1]==0){
				location = "#/datasourceEdit/"+datasourceId+"/"+1;
			}else{
				location = "#/datasourceEdit/"+datasourceId+"/"+pageArray[pageArray.length-1];
			}
		};
		$scope.tog = function(index,$event){
			if($scope.showLineNum == index)
				$scope.showLineNum = null;
			else
				$scope.showLineNum = index;
		};
	}]);
});