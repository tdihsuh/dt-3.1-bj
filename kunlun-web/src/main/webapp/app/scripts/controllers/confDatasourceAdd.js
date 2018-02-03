define(['app'],function(app){
	app.controller('confDatasourceAddCtrl',['$scope','$http','$routeParams','$rootScope',function($scope,$http,$routeParams,$rootScope){
		$rootScope.app_nav = "首页 / 配置管理 / 数据源配置";
		alert("dsfsf");
		var currentPage = $routeParams.param;
		$scope.showData = 1;
		$scope.showLine = null;
		$scope.showNext = 1;
		$scope.showDetermine = null;
		$scope.protoc = null;
		$scope.showDrevious = null;
		var urlCg = "datasource/categoryList.hs";
		$http.get(urlCg).success(function(data){
			var categ = data.model.dataCategoryList;
			$scope.category = categ;
		});
		
		var ag;
		var urlAg = "datasource/agentList.hs";
		$http.post(urlAg).success(function(data){
			ag = data.model.dataAgentList;
			//alert(ag.length);
			if(ag.length>0){
				$scope.agent = ag;
			}else{
				$("#agentError").html("<font color='red'>请先添加采集器之后，再添加数据源</font>");
			}
			
		});
		var fw;
		var urlFw = "datasource/forwarderList.hs";
		$http.get(urlFw).success(function(data){
			fw = data.model.dataForwarderList;
			if(fw.length>0){
				$scope.forwarder = fw;
			}else{
				$("#forwarderError").html("<font color='red'>请先添加转发器之后，再添加数据源</font>");
			}
			
		});
		
		
		$scope.selChange = function(id){
			$("#categoryError").html("");
		    var urlC = "datasource/category.hs?id="+id;
			$http.post(urlC).success(function(data){
				var acs = data.model.category;
				if(acs.name=='other'){
					$("#categoryType").show();
				}else{
					$("#categoryType").hide();
				}
				$("#category1").val(acs.name);
				$("#type1").val(acs.type);
				//$("#pattern").val(acs.pattern);
				//$("#agentParser").val(acs.agentParser);
				//$("#forwarderParser").val(acs.forwarderParser);
				$("#example").val(acs.example);
			});		
		};
		$scope.typeChange = function(type){
			if(type != ""){
				$("#typeError").html("");
			}
		};
		$scope.dNChange = function(datasourceName){
			//console.log(datasourceName);
			if(datasourceName == ""){
				$("#datasourceNameError").html("<font color='red'>数据源名称不能为空 请重新输入</font>");
			}else{
				$("#datasourceNameError").html("");
			}
//			var regex = /^[a-zA-Z0-9_\一-\龥]+$/;
//			var reg = /^[^ /\n]+$/;
//			var reg1 =new RegExp("(^\s*)|(\s*$)"); 
//			console.log(!reg1.test(datasourceName));
//			if(!reg1.test(datasourceName)){  
//				$("#datasourceNameError").html("<font color='red'>数据源名称错误 请重新输入</font>");
//				return false;
//			}else{
//				$("#datasourceNameError").html("");
//			}  
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
		$scope.cNChange = function(categoryName){
			if(categoryName != ""){
				$("#categoryNameError").html("");
			}
		};
		
		var protocolStr;
		$scope.proChange = function(protocol){
			protocolStr = protocol;
			if(protocol!=""){
				$("#protocolError").html("");
			}
		};
		$scope.agentParserChange = function(agentParser){
			if(agentParser!=""){
				$("#agentParserError").html("");
			}
		};
		$scope.forwarderParserChange = function(forwarderParser){
			if(forwarderParser!=""){
				$("#forwarderParserError").html("");
			}
		};
		
		/***$scope.patternChange = function(pattern){
			if(pattern !=""){
				$("#patternError").html("");
			}else{
				$("#patternError").html("<font color='red'>解析规则不能为空, 请重新输入</font>");
			}
		};**/
		$scope.urlChange = function(url){
			if(url !=""){
				$("#urlError").html("");
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
		$scope.encodingChange = function(encoding){
			if(encoding !=""){
				$("#encodingError").html("");
			}
		};
		
		
		$scope.hostChange = function(host){
			var reHost =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;  
			if(!reHost.test(host)){  
				$("#hostError").html("<font color='red'>主机错误 请重新输入</font>");
				return false;
			}else{
				$("#hostError").html("");
			}
			if(host ==""){
				$("#hostError").html("<font color='red'>主机不能为空, 请重新输入</font>");
			}else{
				$("#hostError").html("");
			}
		};
		$scope.portChange = function(port){
			
			var reProt = /^\+?[1-9][0-9]*$/;
			if(!reProt.test(port)){  
				$("#portError").html("<font color='red'>端口错误 请重新输入</font>");
				return false;
			}else{
				$("#portError").html("");
			}
			if(port==""){
				$("#portError").html("<font color='red'>端口不能为空</font>");
			}else{
				$("#portError").html("");
			}
		};
		var agentName =[];
		$scope.agentChange2 = function(id){
			if(id !=""){
				$("#agent2Error").html("");
			}
		    var urlC = "datasource/agent.hs?id="+id;
			$http.post(urlC).success(function(data){
				var dataAgent = data.model.agent;
				if(agentName.length<2){
					agentName.push(dataAgent.name);
				}
			});		
		};
		var forwarderName =[];
		var forwarder = [];
		$scope.forIdChange = function(id,name){
			forwarderName.push(name);
			if(id !=""){
				$("#forwarderIdError").html("");
			}
			if(forwarderName.length<=5){
				$("#forwarderIdError").html("");
			}else{
				$("#forwarderIdError").html("<font color='red'>转发器最多只能选择5个，请重新选择</font>");
			}
			if(forwarderName.length>6){
				$("#forwarderIdError").html("");
				var count = 0;
				for(var i=0;i<forwarderName.length;i++){
					if(forwarderName[i]!=name){
						if(forwarder.length<=5){
							forwarder[count++]=forwarderName[i];
						}
					}
				}
			}else{
				forwarder.push(name);
			}
		};
		var wz = 0;
		$scope.nextDS = function() {
				
			wz++;
			if(wz==1){
				if(fw.length<0){
					$("#forwarderError").html("<font color='red'>请先添加转发器之后，再添加数据源</font>");
					wz=0;
				}
				if(ag.length<0){
					$("#agentError").html("<font color='red'>请先添加采集器之后，再添加数据源</font>");
					wz=0;
				}
				var categoryValue = $("#category").val();
				var typeValue = $("#type").val();
				var categoryNameValue = $("#categoryName").val();
				var datasourceNameValue = $("#datasourceName").val();
				if(datasourceNameValue == ""){
					$("#datasourceNameError").html("<font color='red'>数据源名称不能为空，请重新输入！！</font>");
					wz=0;
				}else{
					$("#datasourceNameError").html("");
				}
				if(categoryValue == ""){
					$("#categoryError").html("<font color='red'>类型不能为空，请重新选择！！</font>");
					wz=0;
				}else if(categoryValue == "8"){
					if(typeValue != "" ){
						$("#typeError").html("");
					}else{
						$("#typeError").html("<font color='red'>日志类型不能为空，请重新选择！！</font>");
						wz=0;
					}
					if(categoryNameValue != ""){
						$("#categoryNameError").html("");
					}else{
						$("#categoryNameError").html("<font color='red'>日志名称不能为空，请重新输入</font>");
						wz=0;
					}
					if(categoryNameValue!="" && typeValue != "" && datasourceNameValue !=""){
						$scope.showData = 2;
						$scope.showDrevious=2;
					}
					
					
				}else{
					$("#categoryError").html("");
					$("#typeError").html("");
					$("#categoryNameError").html("");
				}
				if(categoryValue != "" && datasourceNameValue != ""){
					$scope.showData = 2;
					$scope.showDrevious=2;
				}
			}
			if(wz==2){
				var protoValue =$("#protocol").val();
				var agentParserValue =$("#agentParser").val();
				var forwarderParserValue =$("#forwarderParser").val();
				if(protoValue != "" ){
					$("#protocolError").html("");
				}else{
					$("#protocolError").html("<font color='red'>协议不能为空，请重新选择！！</font>");
					wz =1;
				}
				if(agentParserValue != ""){
					$("#agentParserError").html("");
				}else{
					$("#agentParserError").html("<font color='red'>采集器解析不能为空，请重新输入！！</font>");
					wz =1;
				}
				if(forwarderParserValue !=""){
					$("#forwarderParserError").html("");
				}else{
					$("#forwarderParserError").html("<font color='red'>转发器解析不能为空，请重新输入！！</font>");
					wz =1;
				}
				if(protoValue == 'file'){
					$scope.showLine = 1;
				}else{
					$scope.showLine = 2;
				}
			}
			if(wz==3){
				var protoValue =$("#protocol").val();
				if(protoValue=="file"){
					//var pattern =$("#pattern").val();
					var urlValue =$("#url").val();
					var encoding =$("#encoding").val();
					//alert(pattern+"++++"+urlValue+"-----"+pattern);
					//alert((pattern != "")+"++++"+(urlValue != "")+"-----"+(encoding != ""));
					//if(pattern != ""){
					//	$("#patternError").html("");
					//}else{
					//	$("#patternError").html("<font color='red'>解析规则不能为空，请重新输入</font>");
					//	wz=2;
					//}
					if(urlValue != ""){
						$("#urlError").html("");
					}else{
						$("#urlError").html("<font color='red'>路径不能为空，请重新输入</font>");
						wz=2;
					}
					if(encoding != ""){
						$("#encodingError").html("");
					}else{
						$("#encodingError").html("<font color='red'>编码格式不能为空，请重新输入</font>");
						wz=2;
					}
				}else{
					//var pattern =$("#pattern").val();
					var host =$("#host").val();
					var port =$("#port").val();
					var encoding =$("#encoding").val();
					//alert(agentHost+"++++"+port+"-----"+encoding2);
					//if(pattern != ""){
					//	$("#patternError").html("");
					//}else{
					//	$("#patternError").html("<font color='red'>解析规则不能为空，请重新输入</font>");
					//	wz=2;
					//}
					if(host != ""){
						$("#hostError").html("");
					}else{
						$("#hostError").html("<font color='red'>主机不能为空，请重新输入</font>");
						wz=2;
					}
					if(port != ""){
						$("#portError").html("");
					}else{
						$("#portError").html("<font color='red'>端口不能为空，请重新输入</font>");
						wz=2;
					}
					if(encoding != ""){
						$("#encodingError").html("");
					}else{
						$("#encodingError").html("<font color='red'>编码格式不能为空，请重新输入</font>");
						wz=2;
					}
				}
				
			}
			//alert(wz);
			if(wz==4){
				 var category2 = $("#category1").val();
				 var agent =$("#agent2").val();
				 var forwarderValue =[];    
				  $('input[name="forwarderId"]:checked').each(function(){    
					  forwarderValue.push($(this).val());    
				  });
				 	if(agent != ""){
						$("#agent2Error").html("");
					}else{
						$("#agent2Error").html("<font color='red'>采集器是必选项不能为空，请重新选择</font>");
						wz=3;
					}
				 	//console.log(forwarderValue.length>0);
					if(forwarderValue.length>0){
						$("#forwarderIdError").html("");
					}else if(forwarderValue.length>5){
						$("#forwarderIdError").html("<font color='red'>转发器最多只能选择5个，请重新选择</font>");
						wz=3;
					}else{
						$("#forwarderIdError").html("<font color='red'>转发器是必选项不能为空，请重新选择</font>");
						wz=3;
					}
					if(forwarderValue.length>0 && agent != ""&& forwarderValue.length<=5){
						$scope.showNext = 2;
						$scope.showDetermine = 2;
					}else{
						$scope.showNext = 1;
						$scope.showDetermine = 1;
						wz=3;
					}
					//console.log($("#category1").val()+","+$("#type1").val()+","+agentName);
					if($("#category1").val() == 'other'){
						$("#cate").html($("#categoryName").val());
						 $("#typ").html($("#type").val());
					}else{
						$("#cate").html($("#category1").val());
						$("#typ").html($("#type1").val());
					}
					  $("#pro").html($("#protocol").val());
					  $("#agen").html($("#agentParser").val());
					  $("#forward").html($("#forwarderParser").val());
					  var patr = [];
					  patr.push($("#pattern").val());
					  if(patr.toString().length>30){
						 $("#patt").html(patr.toString().substring(0, 30)+"...");
					  }else{
						  $("#patt").html($("#pattern").val());
					  }
					 
					  
					  $("#ur").html($("#url").val());
					  $("#ipv4").html($("#host").val());
					  $("#encod").html($("#encoding").val());
					  $("#port1").html($("#port").val());
					  //$("#exam").html($("#example").val());
					  $("#agen2").html(agentName);
					  $("#forwarder2").html(forwarder);
					  $("#catName").val($("#categoryName").val());
					  $("#dataName").html($("#datasourceName").val());
					  var protoc = $("#protocol").val();
					  if(protoc=="file"){
						  $scope.protoc = 1;
					  }else{
						  $scope.protoc = 2;
					  }
			}
			
			
			
			if (wz>4) wz = 4;
			$(".wizard .steps li").removeClass("active").eq(wz).addClass("active");
			$(".step-content .step-pane").hide(0).eq(wz).show(0);
		};
		
		
		
		
		$scope.previous = function() {
			wz--;
			$scope.showNext = 1;
			if(wz==0){
				$scope.showData = 1;
				$scope.showDrevious=1;
				//alert(wz);
				$("#datasourceNameError").html("");
				$("#protocolError").html("");
				$("#agentParserError").html("");
				$("#forwarderParserError").html("");
			}
			if(wz==1){
				$("#patternError").html("");
				$("#urlError").html("");
				$("#encodingError").html("");
				$("#hostError").html("");
				$("#portError").html("");
			}
			//alert(wz);
			if(wz==2){
				$("#agent2Error").html("");
				$("#forwarderIdError").html("");
			}
			
			if(wz==3){
				$scope.showDetermine = 1;
			}
			
			if (wz<0) wz = 0;
			$(".wizard .steps li").removeClass("active").eq(wz).addClass("active");
			$(".step-content .step-pane").hide(0).eq(wz).show(0);
		};
		
		
		/**
		var forwarderId;
		$scope.toggle =function(data){
			forwarderId =data;
			alert(forwarderId);	
		}**/
		var dataAdd = 0;
		$scope.datasourceAdd = function(datasource,categoryName){
			//var currentPage = 1;
			var pageSize = 5;
			dataAdd++;
			if(dataAdd==1){
				  //alert(datasource.datasourceName);
				  var forwarder_value =[];    
				  $('input[name="forwarderId"]:checked').each(function(){    
				   forwarder_value.push($(this).val());    
				  }); 
				  var forwarderId = forwarder_value.length==0 ?'没有选择forwarder':forwarder_value ;  
				  //alert(forwarderId); 
				var dataDo = "?categoryId="+datasource.categoryId+"&type="+datasource.type+"&agentId="+datasource.agentId;
					dataDo +="&host="+datasource.host+"&port="+datasource.port+"&protocol="+datasource.protocol;
					dataDo +="&url="+datasource.url+"&encoding="+datasource.encoding+"&forwarderId="+forwarderId;
					dataDo +="&forwarderParser="+datasource.forwarderParser+"&agentParser="+datasource.agentParser+"&datasourceName="+datasource.datasourceName+"&categoryName="+categoryName;
				var pattern = datasource.pattern;
				var param = {
					  pattern:pattern
					}
				var urlFw = "datasource/addDatasource.hs"+dataDo+"&pattern="+escape(pattern);
				$http.get(urlFw).success(function(data){
					var flag = data.model.flag;
					if(flag==true){
						location = "#/datasource/"+currentPage;
					}else{
						$("#datasourceError").html("<b><font color='red'>后台配置出错了，请检查后台相关的配置！！！</font></b>");
					}
				});
			}
		};
		
		$scope.datasource = function(){
			location = "#/datasource/"+currentPage;
		};
		
		$scope.pattern = function(){
			 $("#patt").html($("#pattern").val());
		};
		$scope.pattern2 = function(){
			 var patr = [];
			 patr.push($("#pattern").val());
			 if(patr.toString().length>30){
				 $("#patt").html(patr.toString().substring(0, 30)+"...");
			 }else{
				 $("#patt").html($("#pattern").val());
			 }
		};		
	}]);
	
});