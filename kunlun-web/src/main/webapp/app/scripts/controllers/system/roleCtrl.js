define(['app'],function(app){
	app.controller("roleCtrl",["$scope","$http","hs_info",function($scope,$http,hs_info){
		
		$scope.role = [];
		
		$http.post("tRole/list.hs").success(function(data){
			$scope.role.list = data.model.roleList;
		});
		
		$scope.role.save = function(){
			var id = $("input[name=id]").val();
			var name = $("input[name=name]").val();
			var description = $("input[name=description]").val();
			
			var role = {
				"id":id,
				"name":name,
				"description":description
			}
			$http.post("tRole/save.hs",role).success(function(data){
				$scope.role.list = data.model.roleList;
			})
			.error(function(e){
				alert(hs_info.errors);
			});
		};
		
		$scope.role.modify = function(role){
			$("input[name=id]").val(role.id);
			$("input[name=name]").val(role.name);
			$("input[name=description]").val(role.description);
		};
		
		$scope.role.del = function(role){
			if(confirm("确定要删除角色 "+role.name+" ？")){
				$http.post("tRole/del.hs",role).success(function(data){
					$scope.role.list = data.model.roleList;
				}).error(function(e){
					alert(hs_info.errors);
				});
			}
		}
		
		$scope.role.clear = function(){
			$("input[name=id]").val("");
			$("input[name=name]").val("");
			$("input[name=description]").val("");
		}
	}])
});