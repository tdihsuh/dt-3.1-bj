define(['app'],function(app){
	app.controller('usermngCtrl',['$scope','$http','hs_info','User','$rootScope',function($scope,$http,hs_info,User,$rootScope){
		$rootScope.app_nav = "首页 / 系统管理 / 用户管理";
		$("#cmbbody").css("overflow","auto");
		$scope.user = {
			"module":"",		//list:用户列表 modify:修改个人信息 add:新增用户
			"roleCheckList":[],		//选中的角色
			"roleList":[],		//角色列表
			"isManager":false	//是否有管理权限
		};
		var user = new User();
		user.query().then(function(u){
			for(var i in u.roles){
				if(u.roles[i].name == "ROLE_ADMIN"){
					$scope.user.isManager = true;
				}
			}
			if($scope.user.isManager){
				$scope.user.isManager = true;
				$scope.user.module = "list";
			}else{
				$scope.user.module = "modify";
				document.modifyForm.reset();
				$("#user_modify_info").html(u.userId+"（"+u.nickName+"）");
				$("input[name=id_]").val(u.id);
				$("input[name=userId_]").val(u.userId);
				$("input[name=email_]").val(u.email);
			}
		});
		$http.post("tRole/list.hs").success(function(data){
			$scope.user.roleList = data.modelMap.roleList;
		});
		$scope.warning = function(){
			if(document.getElementById('iswarning').checked){
				$('#duration,#durationunit').removeAttr('disabled');
			}else{
				$('#duration,#durationunit').attr('disabled','disabled');
			}
		};
		$http.post("tUser/list.hs").success(function(data){
			$scope.user.list = data.model.userList;
		});
		$scope.user.add = function(){
			$scope.user.module = "add";
			document.addForm.reset();
			$scope.user.roleCheckList = [];
		};
		$scope.user.save = function(){
			var userId = $("input[name=userId]").val();
			var nickName = $("input[name=nickName]").val();
			var password = $("input[name=password]").val();
			var email = $("input[name=email]").val();
			
			var user = {
				"userId":userId,
				"nickName":nickName,
				"password":password,
				"email":email,
				"roles":$scope.user.roleCheckList
			}
			if($scope.user.roleCheckList == null || $scope.user.roleCheckList.length == 0){
				alert("请选择用户角色！");
				return false;
			}
			$http.post("tUser/save.hs",user).success(function(data){
				$scope.user.list = data.model.userList;
				$scope.user.module = "list";
			}).error(function(e){
				alert(hs_info.errors);
			});
		};
		$scope.user.update = function(){
			var id = $("input[name=id_]").val();
			var userId = $("input[name=userId_]").val();
			var old_password = $("input[name=old_password_]").val();
			var re_password = $("input[name=re_password_]").val();
			var password = $("input[name=password_]").val();
			var email = $("input[name=email_]").val();
			if(password == re_password){
				document.getElementById("re_password_").setCustomValidity('');
			}else{
				document.getElementById("re_password_").setCustomValidity("密码前后不一致！");
				return false;
			}
			
			var user = {
				"id":id,
				"userId":userId,
				"password":old_password,
				"email":email,
				"roles":$scope.user.roleCheckList
			}
			
			if($scope.user.isManager){//如果当前登录用户为管理员，不需要验证原密码
				if($scope.user.roleCheckList == null || $scope.user.roleCheckList.length == 0){
					alert("请选择用户角色！");
					return false;
				}
				user.password = password;
				$http.post("tUser/save.hs",user).success(function(data){
					$scope.user.list = data.model.userList;
					$scope.user.module = "list";
				}).error(function(){
					alert(hs_info.errors);
				});
			}else{
				$http.post("tUser/checkPassword.hs",user).success(function(data){
					if(eval(data)){
						user.password = password;
						user.roles = null;
						$http.post("tUser/save.hs",user).success(function(data){
							$scope.user.list = data.model.userList;
							$scope.user.module = "modify";
							document.modifyForm.reset();
							$("input[name=email_]").val(email);
						}).error(function(){
							alert(hs_info.errors);
						});
					}else{
						document.getElementById("old_password_").setCustomValidity("密码有误！");
					}
				}).error(function(){
					alert(hs_info.errors);
				});
			}
		}
		$scope.user.del = function(user){
			if(confirm("确定要删除 " +user.userId+ " 用户？")){
				$http.post("tUser/del.hs",user).success(function(data){
					$scope.user.list = data.model.userList;
					$scope.user.module = "list";
				}).error(function(e){
					alert(hs_info.errors);
				});
			}
		};
		$scope.user.modify = function(user){
			$scope.user.module = "modify";
			document.modifyForm.reset();
			$scope.user.roleCheckList = [];
			$("#user_modify_info").html(user.userId+"（"+user.nickName+"）");
			$("input[name=id_]").val(user.id);
			$("input[name=userId_]").val(user.userId);
			$("input[name=email_]").val(user.email);
			for(var i in user.roles){
				for(var j in $scope.user.roleList){
					if(user.roles[i].id == $scope.user.roleList[j].id)
						$scope.user.roleCheckList.push($scope.user.roleList[j]);
				}
			}
		};
		$scope.user.clearOldPasswordValidity = function(){
			document.getElementById("old_password_").setCustomValidity("");
		};
		$scope.user.clearPasswordValidity = function(){
			document.getElementById("password_").setCustomValidity("");
			document.getElementById("re_password_").setCustomValidity("");
		};
		$scope.user.selectRole = function(role){
			var n = $scope.user.roleCheckList.indexOf(role);
			if(n == -1){
				$scope.user.roleCheckList.push(role);
			}else{
				$scope.user.roleCheckList.splice(n,1);
			}
		}
		
		$(function(){
		});
	}])
});