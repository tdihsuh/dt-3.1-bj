define(['app'],function(app){
	app.controller('addAgentCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 采集器配置";
		var currentPage = $routeParams.param;
		var dataAdd = 0 ;
		$scope.addConfAgent = function(agent){
			var pageSize = 5;
			dataAdd++;
//			var regex = /^[a-zA-Z0-9_\一-\龥]+$/;
//			if(!regex.test(agent.name)){  
//				$("#nameError").html("<font color='red'>名称错误 请重新输入</font>");
//				dataAdd=0;
//			}else{
//				$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成");
//			}  
			var re2 =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
			if(!re2.test(agent.ip)){  
				$("#ipError").html("<font color='red'>ip地址错误 请重新输入</font>");
				dataAdd=0;
			}  
			if(agent.name==undefined){
				dataAdd=0;
				$("#nameError").html("<font color='red'>名称不能为空  请重新输入</font>");
			}
			if(agent.ip==undefined){
				dataAdd=0;
				$("#ipError").html("<font color='red'>ip不能为空  请重新输入</font>");
			}
			//alert(dataAdd);
			if(dataAdd==1){
				var url ="agent/add.hs?name="+agent.name+"&ip="+agent.ip+"&description="+agent.description;
					url +="&currentPage="+currentPage+"&pageSize="+pageSize;
				$http.get(url).success(function(data){
					location = "#/confAgent/"+currentPage;
				});
			}
		};
		$scope.ipChange = function(ip){
			//console.log(ip);
			if(ip==undefined){
				$("#ipError").html("<font color='red'>ip不能为空  请重新输入</font>");
				return false;
			}
			var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
			if(!re.test(ip)){  
				$("#ipError").html("<font color='red'>ip地址错误 请重新输入</font>");
			}else{
				$("#ipError").html("<font color='green'></font>");
			}  
		};
		
		
		
		
		$scope.agentChange = function(name){
		    //alert(name);
//			console.log(name);
//			var reg =  /^[a-zA-Z0-9_\一-\龥]+$/ ;  
//			if(!reg.test(name)){  
//				$("#nameError").html("<font color='red'>名称错误 请重新输入</font>");
//				return false;
//			}else{
//				$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成");
//			}  
			if(name == undefined){  
				$("#nameError").html("<font color='red'>名称不能为空, 请重新输入</font>");
				return false;
			}else{
				$("#nameError").html("");
			}  
			
				var url ="agent/agentNameValid.hs?name="+name;
				$http.get(url).success(function(data){
					var confAgent = data.model.confAgent;
					$scope.ConfAgent = confAgent;
					console.log(confAgent);
					if(confAgent==true){
						$("#nameError").html("<font color='red'>名称已存在,请重新输入</font>");
					}else{
						$("#nameError").html("<font color='green'></font>");
						
					}
				});
		};
		$scope.confAgentList = function(){
			location = "#/confAgent/"+currentPage;
		};
	}]);
});