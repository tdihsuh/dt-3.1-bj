define(['app'],function(app){
	app.controller('detailConfAgentCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
			$rootScope.app_nav = "首页 / 配置管理 / 采集器配置";
			var id = $routeParams.param1;
		    var currentPage = $routeParams.param2;
		    var url ="agent/detail.hs?id="+id;
		    var param = {
		    	id:id
		    }
			$http.post(url).success(function(data){
				var acs = data.model.confAgent;
				//alert(acs.id);
				$scope.confAgent = acs;
			});
		   
			$scope.editChange = function(name){
			    //alert(name);
				if(name==undefined){
					$("#nameError").html("<font color='red'>名称不能为空  请重新输入</font>");
					return false;
				}
//				var re =  /^[a-zA-Z0-9_\一-\龥]+$/ ;  
//				if(!re.test(name)){  
//					$("#nameError").html("<font color='red'>名称错误 请重新输入</font>");
//					return false;
//				}else{
//					$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成");
//				}  
				var url ="agent/agentNameValid.hs?name="+name;
				$http.get(url).success(function(data){
					var confAgent = data.model.confAgent;
					$scope.ConfAgent = confAgent;
					if(confAgent==true){
						$("#nameError").html("<font color='red'>名称已存在,请重新输入</font>");
					}else{
						$("#nameError").html("<font color='green'></font>");
						
					}
				});
			};
			
			$scope.ipChange = function(ip){
				var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
				if(ip==undefined){
					$("#ipError").html("<font color='red'>ip不能为空  请重新输入</font>");
					return false;
				}
				if(!re.test(ip)){  
					$("#ipError").html("<font color='red'>ip地址错误 请重新输入</font>");
					return false;
				}else{
					$("#ipError").html("<font color='green'></font>");
				}  
				
			};
			 var dataAdd = 0 ;		
			$scope.editConfAgent = function(ConfAgent){
				var pageSize = 5;
				dataAdd++;
//				var re =  /^(?!_)(?!.*?_$)[a-zA-Z0-9_\一-\龥]+$/ ;  
//				if(!re.test(ConfAgent.name)){  
//					$("#nameError").html("<font color='red'>名称错误 请重新输入</font>");
//					dataAdd=0;
//				}else{
//					$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成不能以下划线开头和结尾");
//				}  
				var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
				if(!re.test(ConfAgent.ip)){  
					$("#ipError").html("<font color='red'>ip地址错误 请重新输入</font>");
					dataAdd=0;
				}  
				if(ConfAgent.name==undefined){
					dataAdd=0;
					$("#nameError").html("<font color='red'>名称不能为空  请重新输入</font>");
				}
				if(ConfAgent.ip==undefined){
					dataAdd=0;
					$("#ipError").html("<font color='red'>ip不能为空  请重新输入</font>");
				}
				if(dataAdd==1){
					var url ="agent/update.hs?name="+ConfAgent.name+"&ip="+ConfAgent.ip+"&description="+ConfAgent.description+"&id="+ConfAgent.id+"&state="+ConfAgent.state;
						url +="&currentPage="+currentPage+"&pageSize="+pageSize+"&createDate="+ConfAgent.createDate;
					$http.get(url).success(function(data){
					location = "#/confAgent/"+currentPage;
					});
				}
		};
		
		$scope.confAgentList = function(){
			location = "#/confAgent/"+currentPage;
		};
	
			
	}]);
});