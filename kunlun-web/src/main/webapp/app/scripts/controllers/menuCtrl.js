define(['app'],function(app){
	app.controller("menuCtrl",["$scope","$http","$location",'User','$rootScope','$timeout','Authority',function($scope,$http,$location,User,$rootScope,$timeout,Authority){
		$scope.user = {
			"isManager":false	//是否有管理权限
		};
		$scope.initMenu = function(){
			var user = new User();
			user.query().then(function(u){
				for(var i in u.roles){
					if(u.roles[i].name == "ROLE_ADMIN"){
						$scope.user.isManager = true;
					}
				}
				if($scope.user.isManager){
					$http.get("app/data/menu.json").success(function(data){
						$scope.menus = data;
					});
				}else{
					$http.get("app/data/userMenu.json").success(function(data){
						$scope.menus = data;
					});
				}
			});
			
		};
		$scope.initMenu();
		$scope.showSubMenu = function(curr_menu){
			var nav = "首页";
			for(var i=0;i<$scope.menus.length;i++){
				if(curr_menu != $scope.menus[i])
					$("#menu_"+$scope.menus[i].id).hide();
			}
			if(curr_menu.subMenus && curr_menu.subMenus.length > 0){//如果有子菜单，显示子菜单
				if($("#menu_"+curr_menu.id).css("display") == "none")
					$("#menu_"+curr_menu.id).show();
				else
					$("#menu_"+curr_menu.id).hide();
			}else{//否则打开连接
				nav += " / " + curr_menu.menuName;
//				$rootScope.app_nav = nav;
				$location.path(curr_menu.url);
			}
		};
		$scope.openSubMenu = function(subMenu){
			var nav = "首页";
			for(var i=0;i<$scope.menus.length;i++){
				if($scope.menus[i].id == subMenu.parentId){
					nav += " / "+$scope.menus[i].menuName;
				}
			}
			nav += " / " + subMenu.menuName
//			$rootScope.app_nav = nav;
			$location.path(subMenu.url);
		}
		$scope.toggleMenu = function(){
			if($(".menu_span").css("display") == "none"){
				showMenu();
			}else{
				hideMenu();
			}
		}
		$scope.showFloatSubMenu = function(menu){
			$(".float_menu").remove();
			if($(".menu_span").css("display") != "none") return;
			var p = $("#menu"+menu.id).position();
			var str = "<div class='float_menu btn-group open' id='float_menu_"+menu.id+"' style='top:"+p.top+"px'>";
			str += "<ul class='dropdown-menu'>";
			if(!!menu.url){
				str += "<li class='float_menu_title' onclick=\"location='#"+menu.url+"';\">"+menu.menuName+"</li>";
			}else{
				str += "<li class='float_menu_title'>"+menu.menuName+"</li>";
			}
			if(menu.isParent){
				for(var i=0;i<menu.subMenus.length;i++){
					if(!!menu.subMenus[i].url){
						str += "<li class='float_menu_comm' onclick=\"location='#"+menu.subMenus[i].url+"';\">"+menu.subMenus[i].menuName+"</li>";
					}else{
						str += "<li class='float_menu_comm'>"+menu.subMenus[i].menuName+"</li>";
					}
				}
			}
			str += "</ul>";
			str += "</div>";
			$("#menu"+menu.id).append(str);
		}
		var hideFloatSubMenu = function(){
			$(".float_menu").remove();
		}
		var showMenu = function(){
			$(".menu_span").css("display","");
			$("#menu_pan_i").attr("class","fa fa-arrow-circle-left hit");
			$(".layout_left_2").attr("class","layout_left_1");
			$(".layout_right_2").attr("class","layout_right_1");
			$("#menu_pan").attr("class","menu_pan");
		}
		var hideMenu = function(){
			$(".menu_span").css("display","none");
			$("#menu_pan_i").attr("class","fa fa-arrow-circle-right hit");
			$(".layout_left_1").attr("class","layout_left_2");
			$(".layout_right_1").attr("class","layout_right_2");
			$("#menu_pan").attr("class","menu_pan_1");
		}
		$scope.timeout = function(){
			timerId = $timeout(function(){
				$http.post('tUser/isLogin.hs').success(function(data){
					if(!eval(data))
						location = "login.hs";
				});
				$scope.timeout();
			},60000);
		};
		$scope.$on('$destroy', function() {
			$timeout.cancel(timerId);
		});
		$scope.timeout();
		$(function(){
			$("body").bind("click",function(){
				hideFloatSubMenu();
			});
		});
	}])
});