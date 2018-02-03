define(['app'],function(app){
	app.controller('confForwarderAddCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 转发器配置";	
		var currentPage = $routeParams.param;
		$scope.forwarderChange = function(name){
		    //alert(name);
//			var re = /^[a-zA-Z0-9_\一-\龥]+$/ ;  
//			if(!re.test(name)){  
//				$("#nameError").html("<font color='red'>名称错误,请重新输入</font>");
//				return false;
//			}else{
//				$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成");
//			}  
			if(name==undefined){
				$("#nameError").html("<font color='red'>名称不能为空 ,请重新输入</font>");
				return false;
			}
			var url ="forwarder/forwarderNameValid.hs?name="+name;
			$http.get(url).success(function(data){
				var confForwarder = data.model.confForwarder;
				$scope.confForwarder = confForwarder;
				//console.log(confForwarder);
				if(confForwarder==true){
					$("#nameError").html("<font color='red'>名称已存在,请重新输入</font>");
				}else{
					$("#nameError").html("<font color='green'></font>");
					
				}
			});
		};
		$scope.ipChange = function(ip){
			//console.log(ip);
			var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
			if(!re.test(ip)){  
				$("#ipError").html("<font color='red'>ip地址错误,请重新输入</font>");
			}else{
				//$("#ipError").html("<font color='red'>&nbsp;*</font>ip地址，如：192.168.1.1");
				$("#ipError").html("");
			} 
			if(ip==undefined){
				$("#ipError").html("<font color='red'>ip不能为空 ,请重新输入</font>");
				return false;
			}
		};

		 var dataAdd = 0 ;	
		$scope.forwarderAdd = function(forwarder){
			var pageSize = 5;
			dataAdd++;
//			var re = /^[a-zA-Z0-9_\一-\龥]+$/ ;  
//			if(!re.test(forwarder.name)){  
//				$("#nameError").html("<font color='red'>名称错误,请重新输入</font>");
//				dataAdd=0;
//			}else{
//				$("#nameError").html("<font color='red'>&nbsp;*</font>由汉字、数字、字母、下划线组成");
//			}  
				var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
				if(!re.test(forwarder.ip)){  
					$("#ipError").html("<font color='red'>ip地址错误,请重新输入</font>");
					dataAdd=0;
				}  
				if(forwarder.name==undefined){
					dataAdd=0;
					$("#nameError").html("<font color='red'>名称不能为空 ,请重新输入</font>");
				}
				if(forwarder.ip==undefined){
					dataAdd=0;
					$("#ipError").html("<font color='red'>ip不能为空 ,请重新输入</font>");
				}
				if(dataAdd==1){
					var url ="forwarder/add.hs?name="+forwarder.name+"&ip="+forwarder.ip+"&description="+forwarder.description;
						url +="&currentPage="+currentPage+"&pageSize="+pageSize;
					$http.get(url).success(function(data){
						location = "#/forwarder/"+currentPage;
					});
				}
			};
			$scope.forwarderList = function(){
				location = "#/forwarder/"+currentPage;
			};
		
	}]);
});