define(['app'],function(app){
	app.controller('confAgentCtrl',['$scope','$http','$window','$routeParams','$rootScope',function($scope,$http, $window,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 采集器配置";
		$("#cmbbody").css("overflow","auto");
		var currentPage = $routeParams.param;
		var pageSize = 5;
		var pageArray = [];
		if(currentPage==undefined){
			currentPage = 1;
		}
		$scope.queryAgentList = function(currentPage,pageSize){
			var urlAgent = "agent/list.hs?currentPage="+currentPage+"&pageSize="+pageSize;
			$http.get(urlAgent).success(function(data){
				$scope.agent =  data.model.agentList;
				pageArray.push(data.model.agentList.currentPage);
			});
		};
		
		$scope.queryAgentList(currentPage,pageSize);
		
		$scope.queryPage = function(currentPage){
			pageArray.push(currentPage);
			$scope.queryAgentList(currentPage,pageSize);
		};
		
		$scope.add = function(){
			if(pageArray[pageArray.length-1]==0){
				location = "#/addAgent/1";
			}else{
				location = "#/addAgent/"+pageArray[pageArray.length-1];
			}
		};
		$scope.del = function(id){
			var currentPage = pageArray[pageArray.length-1];
			if(currentPage==0){
				currentPage = 1;
			}
			var r=confirm("确定删除该条问题？");
			if(r==true){
				var url ="agent/del.hs?id="+id+"&currentPage="+currentPage+"&pageSize="+pageSize;
				$http.get(url).success(function(data){
					$scope.agent = data.model.agentList;
					var flag = data.model.flag;
					var flag1 = data.model.flag1;
					if(flag==true){
						if(flag1==true){
							
						}else{
							$("#confAgentError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
						}
					}else{
						$("#confAgentError").html("<b><font color='red'>该条采集器正在使用，请勿删除！！！</font></b>");
					}
					
				});
			}
		};
		$scope.edit = function(agentId){
		    if(pageArray[pageArray.length-1]==undefined){
				location = "#/detailAgent/"+agentId+"/"+1;
			}else{
				location = "#/detailAgent/"+agentId+"/"+pageArray[pageArray.length-1];
			}
		};
		$scope.agentLoad = function(id){
			//alert(id);
			var url ="agent/agentLoad.hs?id="+id;
			$window.open(url);
			
//			$http.get(url).success(function(data){
//				var acs = data.model.agentList;
//				$scope.agent = acs;
//			});
		};	
		
		
		
		
	}]);
});