define(['app'],function(app){
	app.controller('systemLogCtrl',['$scope','$http','$window','$rootScope',function($scope,$http, $window,$rootScope){
		$rootScope.app_nav = "首页 / 系统管理 / 系统日志管理";
		$("#cmbbody").css("overflow","auto");
		if(!$scope.pager){
			$scope.pager = {
				"currentPageNum":1,
				"totalPages":0,
				"pageSize":10,
				"startDate":"",
				"endDate":"",
				"summary":"",
				"logs":new Array(),
				"showPagesNum":new Array(),
				"showPagesMax":5,
				"showPagesOrder":1
			};
		};
		// 全选
		$scope.selectAll="";
		// 删除项列表
		$scope.ids=new Array();
		/**
		 * 查询
		 */
		$scope.query = function(mpager){
			mpager.logs=[];
			$scope.ids=[];
			$http.post("systemLog/list.hs",mpager).success(function(data){
				$scope.pager = data.model.pager;
				$scope.pager.logs = data.model.pager.logs;
				$scope.selectAll = false;
			});
		}
		$scope.query($scope.pager);
		
		/**
		 * 搜索
		 */
		$scope.search = function(){
			var fromdate = $("#datepicker_from").val();
			var fromtime = $("#datepicker_time_from").val();
			var todate = $("#datepicker_to").val();
			var totime = $("#datepicker_time_to").val();
			var dfromtime = "00:00:00";
			var dtotime = "23:59:59";
			$scope.pager.currentPageNum = 1;
			$scope.pager.showPagesOrder = 1;
			if(fromdate != ""){
				if(fromtime == ""){
					$scope.pager.startDate = fromdate + " " + dfromtime;
				}else{
					$scope.pager.startDate = fromdate + " " + fromtime;
				}
			}else{
				$scope.pager.startDate="";
			}
			if(todate != ""){
				if(totime == ""){
					$scope.pager.endDate = todate + " " + dtotime;
				}else{
					$scope.pager.endDate = todate + " " + totime;
				}
			}else{
				$scope.pager.endDate="";
			}
			$scope.query($scope.pager);
		}
		
		/**
		 * 页码点击事件
		 */
		$scope.queryByShowNum = function(showNum){
			$scope.pager.currentPageNum = showNum;
			$scope.query($scope.pager);
		}
		
		/**
		 * 页数栏切换事件
		 */
		$scope.queryByShowOrder = function(showOrder){
			if(showOrder > $scope.pager.showPagesOrder){
				$scope.pager.currentPageNum = $scope.pager.showPagesOrder * $scope.pager.showPagesMax + 1;
			}else{
				$scope.pager.currentPageNum = (showOrder-1) * $scope.pager.showPagesMax + 1;
			}
			$scope.pager.showPagesOrder = showOrder;
			$scope.query($scope.pager);
		}
		/**
		 * 删除
		 */
		$scope.del = function(){
			if($scope.ids.length > 0){
				if(confirm("确定要删除选中项？")){
					$scope.pager.logs = [];
					$http({
						url:"systemLog/del.hs",
						method:"POST",
						data:{
							"pager":$scope.pager,
							"ids":$scope.ids
							}
					}).success(function(data){
						$scope.pager = data.model.pager;
						$scope.pager.logs = data.model.pager.logs;
						$scope.selectAll = false;
					});
				}
			}else{
				alert("请先选择要删除项！");
			}
		};
		/**
		 * 全选
		 */
		$scope.checkAll = function(){
			angular.forEach($scope.pager.logs, function (item) {
				if($scope.selectAll){
					if($scope.ids.indexOf(item.id) < 0){
						$scope.ids.push(item.id);
					}
	            }else{
	            	$scope.ids.splice(0);
	            }
				item.selected = $scope.selectAll;
	        });
		}
		/**
		 * 单选
		 */
		$scope.checkOne = function(item){
			if(item.selected){
				if($scope.ids.indexOf(item.id) < 0)
					$scope.ids.push(item.id);
			}else{
				if($scope.ids.indexOf(item.id) > -1)
				$scope.ids.splice($scope.ids.indexOf(item.id),1);
			}
		}
		/**
		 * 时间比较器
		 */
		function comptime() {
		    var beginTime = "2014-09-21";
		    var endTime = "2014-09-21";
		    var beginTimes = beginTime.substring(0, 10).split('-');
		    var endTimes = endTime.substring(0, 10).split('-');

		    beginTime = beginTimes[1] + ' ' + beginTimes[2] + ' ' + beginTimes[0] + beginTime.substring(10, 19);
		    endTime = endTimes[1] + ' ' + endTimes[2] + ' ' + endTimes[0] + endTime.substring(10, 19);

		    console.log(beginTime + "<====>" + endTime);
		    console.log(Date.parse(endTime));
		    console.log(Date.parse(beginTime));
		    var a = (Date.parse(endTime) - Date.parse(beginTime)) / 3600 / 1000;
		    if (a < 0) {
		    	console.log("endTime小!");
		    } else if (a > 0) {
		    	console.log("endTime大!");
		    } else if (a == 0) {
		    	console.log("时间相等!");
		    } else {
		    	console.log('exception');
		    }
		}
		/**
		 * 日期控制
		 */
		$(function(){
			$("#datepicker_from").datepicker({ changeMonth:true,changeYear: true , dateFormat:"yy-mm-dd"});
			$("#datepicker_to").datepicker({ changeMonth:true,changeYear: true , dateFormat:"yy-mm-dd"});
		});
	}])
});