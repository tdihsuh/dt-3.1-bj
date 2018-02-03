define(['app'],function(app){
	app.controller('confWarningCtrl',['$scope','$http','$compile','$window','$rootScope',function($scope,$http, $compile,$window,$rootScope){
		$rootScope.app_nav="首页/ 配置管理/ 预警配置";
		$("#cmbbody").css("overflow","auto");
		$scope.item = [];
		if(!$scope.total){
			$scope.total = {
				"currentPageNum":1,
				"totalPages":0,
				"pageSize":1,
				"cws":new Array(),
				"fuzzy":"",
				"showPagesNum":new Array(),
				"showPagesMax":5,
				"showPagesOrder":1
			};
		};
		var emailReg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
		//var valueReg = "^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$";
		var valueReg = /^(\-|\+)?[\d]{1,10}\.[\d]{1,10}$/;
		/**
		 * 查询
		 */
		
		$scope.query=function(total){
			var url = "warning/list.hs";
			$http.post(url,total).success(function(data){
				
				$scope.total = data.model.total;
				$scope.total.cws = data.model.total.cws;
			});
		
		};
		$scope.query($scope.total);
		/**
		 * 预警配置更新
		 */
		$scope.editConfWarning = function(confWarningId,tag){
			var emailId = "#" + "email_" + tag.getAttribute('id');
		    var valueId = "#" + "value_" + tag.getAttribute('id');
		    var button1 = "#" + "button1_" + tag.getAttribute('id');
		    var button2 = "#" + "button2_" + tag.getAttribute('id');
		    var emailValue = $(emailId).text();
		    var vValue = $(valueId).text();
		    
		    $(emailId).empty();
		    $(valueId).empty(); 
		    $(emailId).append("<input  align='center' name='email'  type='text'   ng-model='item.email' ng-change='emailChange(item.email)' maxlength='55'/><span id='emailError'></span>");
		    $(valueId).append("<input  align='center' name='value'  type='text'   ng-model='item.value' ng-change='valueChange(item.value)' maxlength='4'/><span id='valueError'></span>");
		    $(tag).attr("ng-show",false);
		    $(button1).attr("ng-show",true);
		    $(button2).attr("ng-show",true);
		    $scope.item.email = emailValue;
		    $scope.item.value = vValue;
		    $scope.item.id = confWarningId;
		    $compile($(emailId).contents())($scope);
		    $compile($(valueId).contents())($scope);
		    $compile($("#"+confWarningId).contents())($scope);
		   
		};
		$scope.confWarningEdit = function(tag){
			var email = $scope.item.email;
			var value = $scope.item.value;
			//var reg = /^w+((-w+)|(.w+))*@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+)*.[A-Za-z0-9]+$/;
			var valued = parseInt(value);
			var  url ="warning/update.hs?email="+$scope.item.email+"&id="+$scope.item.id+"&value="+$scope.item.value;
			var flag = true;
			if(!emailReg.test(email.trim())){
				alert(email);
				flag=false;
				$("#emailError").html("<font color='red'>请输入正确的邮箱格式</font>");
			}
			else{
				$("#emailError").html("<font color='green'></font>");
			} 
			if(!valueReg.test(value) || valued >= 1){
				
				flag = false;
				$("#valueError").html("<font color='red'>请输入正确的格式(0.00)</font>");
			}
			else{
				$("#valueError").html("<font color='green'></font>");
			}
			if(flag){	
				$http({
			
				url:url,
				method:"POST",
				data:{
					"total":$scope.total,
					}
				}).success(function(data){
				$scope.total = data.model.total;
				$scope.total.cws = data.model.total.cws;
				});
			}
		};
		
			$scope.emailChange = function(email){
				if(!emailReg.test(email)){  
					$("#emailError").html("<font color='red'>请输入正确的邮箱地址</font>");
				}else{
					$("#emailError").html("<font color='green'></font>");
				}  
			}
			$scope.valueChange = function(value){
				var valued = parseInt(value);
				if(!valueReg.test(value) || valued >= 1){  
					$("#valueError").html("<font color='red'>请输入正确的格式(0.00)</font>");
				}else{
					$("#valueError").html("<font color='green'></font>");
				}  
			}
		    //$compile($("#btn2").contents())($scope);
		
		$scope.confWarningEditCancel = function(tag){
			$scope.query($scope.total);
		}
		/**
		 * 搜索
		 */
		$scope.search = function(fuzzy){
			$scope.total.currentPageNum = 1;
			$scope.total.showPagesOrder = 1;
			$scope.query($scope.total);
		}
		
		
		/**
		 * 页码点击事件
		 */
		$scope.queryByShowNum = function(showNum){
			$scope.total.currentPageNum = showNum;
			$scope.query($scope.total);
		}
		
		/**
		 * 页数栏切换事件
		 */
		$scope.queryByShowOrder = function(showOrder){
			if(showOrder > $scope.total.showPagesOrder){
				$scope.total.currentPageNum = $scope.total.showPagesOrder * $scope.total.showPagesMax + 1;
			}else{
				$scope.total.currentPageNum = (showOrder-1) * $scope.total.showPagesMax + 1;
			}
			$scope.total.showPagesOrder = showOrder;
			$scope.query($scope.total);
		}
		/**
		 * 恢复出厂设置
		 */
	    $scope.editReduction = function(){
	    	var url = "warning/reduction.hs"
	    		$http.post(url,$scope.total).success(function(data){
	    			$scope.total = data.model.total;
					$scope.total.cws = data.model.total.cws;
	    	});
	    	
	    } 

	}]);
});