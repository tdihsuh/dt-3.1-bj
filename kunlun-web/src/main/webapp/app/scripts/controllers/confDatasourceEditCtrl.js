define(['app'],function(app){
	app.controller('confDatasourceEditCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
			$rootScope.app_nav = "首页 / 配置管理 / 数据源配置";
			$scope.show = null;
			$scope.dataCategory = null;
			$scope.agentSelect = null;
			var id = $routeParams.param1;
			var currentPage = $routeParams.param2;
		    var url ="datasource/editDatasource.hs?id="+id;
		    var param = {
		    	id:id
		    };
			$http.post(url).success(function(data){
				var acs = data.model.confDatasource;
				//alert(acs.pattern);
				$scope.datasource = acs;
				if(acs.protocol=='file'||acs.protocol=='hdfs'){
					$scope.show = 1;
					$scope.agentSelect = 1;
				}else{
					$scope.show = 2;
					$scope.agentSelect = 2;
				}
				if(acs.categoryId=='f3dcf7d7-6d9c-41aa-bdce-4afc92df1b6a'){
					$scope.dataCategory = 1;
				}else{
					$scope.dataCategory = 2;
				}
			});
			var urlCg = "datasource/categoryList.hs";
			$http.get(urlCg).success(function(data){
				var categ = data.model.dataCategoryList;
				$scope.category = categ;
			});
			$scope.selChange = function(id){
				if(id==null){
					$("#categoryError").html("<font color='red'>数据源类型不能为空 请重新选择</font>");
					return false;
				}else{
				    var urlC = "datasource/category.hs?id="+id;
					$http.post(urlC).success(function(data){
						var acs = data.model.category;
						if(acs.name == 'other'){
							$("input[name=type]").attr("readonly",false);
							$scope.dataCategory = 1;
						}else{
							$("input[name=type]").attr("readonly",true);
							$("input[name=type]").val(acs.type);
							$scope.dataCategory = 2;
						}
						$("#categoryError").html("");
					});	
				}
			};
			$scope.typeChange = function(type){
				if(type != ""){
					$("#typeError").html("");
				}else{
					$("#typeError").html("<font color='red'>日志类型不能为空 请重新选择</font>");
				}
			};
			var urlAg = "datasource/agentList.hs";
			$http.post(urlAg).success(function(data){
				var ag = data.model.dataAgentList;
				$scope.agent = ag;
			});
			var urlF = "datasource/forwarderList.hs";
			$http.get(urlF).success(function(data){
				var fw = data.model.dataForwarderList;
				$scope.forwarder = fw;
			});
			var urlFDw = "datasource/confDsForwarderList.hs?id="+id;
			var param = {
		    	id:id
		    }
			$http.post(urlFDw).success(function(data){
				var fw = data.model.confDsForwarderList;
				$scope.confDsForwarderList = fw;
			});
			$scope.dNChange = function(datasourceName){
				if(datasourceName == undefined){
					$("#datasourceNameError").html("<font color='red'>数据源名称不能为空 请重新输入</font>");
					return false;
				}else{
					$("#datasourceNameError").html("");
				} 
				var url ="datasource/datasourceNameValid.hs?datasourceName="+datasourceName;
				$http.get(url).success(function(data){
					var datasourceName = data.model.dataSourceName;
					if(datasourceName==true){
						$("#datasourceNameError").html("<font color='red'>数据源名称已存在,请重新输入</font>");
					}else{
						$("#datasourceNameError").html("");
						
					}
				});
			};
			$scope.agentParserChange = function(agentParser){
				if(agentParser!=""){
					$("#agentParserError").html("");
				}else{
					$("#agentParserError").html("<font color='red'>采集器解析不能为空,请重新选择</font>");
				}
			};
			$scope.forwarderParserChange = function(forwarderParser){
				if(forwarderParser!=""){
					$("#forwarderParserError").html("");
				}else{
					$("#forwarderParserError").html("<font color='red'>转发器解析不能为空,请重新选择</font>");
				}
			};
			//$scope.patternChange = function(pattern){
			//	if(pattern !=""){
			//		$("#patternError").html("");
			//	}else{
			//		$("#patternError").html("<font color='red'>解析规则不能为空, 请重新输入</font>");
			//	}
			//};
			$scope.urlChange = function(url){
				if(url !=""){
					$("#urlError").html("");
				}else{
					$("#urlError").html("<font color='red'>路径不能空,请重新输入</font>");
					return false;
				}
				var url1 ="datasource/datasourceUrlValid.hs?url="+url;
				$http.get(url1).success(function(data){
					var dataSourceUrl = data.model.dataSourceUrl;
					if(dataSourceUrl==true){
						$("#urlError").html("<font color='red'>路径已存在,请重新输入</font>");
					}else{
						$("#urlError").html("");
						
					}
				});
			};
			$scope.hostChange = function(host){
				var re =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
				if(host ==""){
					$("#hostError").html("<font color='red'>主机不能为空, 请重新输入</font>");
					return false;
				}
				if(!re.test(host)){  
					$("#hostError").html("<font color='red'>主机错误 请重新输入</font>");
					return false;
				}else{
					$("#hostError").html("");
				}
				
				
			};
			$scope.portChange = function(port){
				var re = /^\+?[1-9][0-9]*$/;
				if(port==""){
					$("#portError").html("<font color='red'>端口不能为空</font>");
					return false;
				}
				if(!re.test(port)){  
					$("#portError").html("<font color='red'>端口错误 请重新输入</font>");
					return false;
				}else{
					$("#portError").html("");
				}
				
			};
			$scope.agentChange2 = function(id){
				if(id !=null){
					$("#agent2Error").html("");
				}else{
					$("#agent2Error").html("<font color='red'>采集器不能为空，请重新选择</font>");
				}
			    
			};
			$scope.encodingChange = function(encoding){
				if(encoding !=undefined){
					$("#encodingError").html("");
				}else{
					$("#encodingError").html("<font color='red'>编码格式不能为空，请重新选择</font>");
				}
			};
			$scope.forIdChange = function(id,name){
				if(id !=""){
					$("#forwarderIdError").html("");
				}
				
			};
			var dataAdd = 0;
			$scope.datasourceEdit = function(datasource){
				var pageSize = 5;
//				var forwarder_value =[];   
//				var forwarderId =  {};
//				  $('input[name="forwarderId"]:checked').each(function(){    
//				   forwarder_value.push($(this).val());    
//				  }); 
//				 if(forwarder_value.length==0){
//					 $("#forwarderIdError").html("<font color='red'>转发器不能为空，请重新选择</font>"); 
//					 return false;
//				 }else if (forwarder_value.length>5){
//					 $("#forwarderIdError").html("<font color='red'>转发器最多只能选择5个，请重新选择</font>"); 
//					 return false;
//				 }else{
//					 forwarderId = forwarder_value; &forwarderId="+forwarderId+"
//				 }
//				 console.log(forwarderId);
				dataAdd++;
				if(dataAdd==1){
					var dataDo = "?id="+datasource.id+"&categoryId="+datasource.categoryId+"&type="+datasource.type+"&agentId="+datasource.agentId;
						dataDo +="&host="+datasource.host+"&port="+datasource.port+"&protocol="+datasource.protocol+"&config="+datasource.config;
						dataDo +="&url="+datasource.url+"&encoding="+datasource.encoding+"&category="+datasource.category;
						dataDo +="&forwarderParser="+datasource.forwarderParser+"&agentParser="+datasource.agentParser+"&state="+datasource.state+"&createDate="+datasource.createDate+"&datasourceName="+datasource.datasourceName;
					var pattern = datasource.pattern;
					var param = {
						  pattern:pattern
						}
				var urlFw = "datasource/updateDatasource.hs"+dataDo+"&pattern="+escape(pattern);
				$http.post(urlFw).success(function(data){
					var flag = data.model.flag;
					if(flag==true){
						//$("#datasourceError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
						location = "#/datasource/"+currentPage;
					}else{
						$("#datasourceError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
					}
					
				});
			}
		};
		
		$scope.datasourceList = function(){
			location = "#/datasource/"+currentPage;
		};
		$scope.proChange = function(protocol){
			if(protocol=='file' || protocol=='hdfs'){
				$scope.show = 1;
				$scope.agentSelect = 1;
				$("#protocolError").html("");
			}else{
				if(protocol==""){
					$("#protocolError").html("<font color='red'>协议不能为空，请重新选择</font>");
				}else{
				$scope.show = 2;
				$scope.agentSelect = 2;
				$("#protocolError").html("");
				}
			}
		};
		
			
			
			
			
			
			
	}]);
});