define(['app'],function(app){
	app.factory('MenuItems',['$http','$q',function($http,$q){
		var menu = {};
		var url = "app/data/menu.json";
		menu.items = [];
		menu.get = function(menuId){
			var deferred = $q.defer();
			$http.post(url).success(function(data){
				menu.items = getMenu(data,menuId);
				if(!menu.items){
					for(var i=0;i<data.length;i++){
						if(data[i].subMenus){
							menu.items = getMenu(data[i].subMenus,menuId);
						}
					}
				}
				console.log(menu.items);
			});
			return deferred.promise;
		};
		var getMenu = function(data,menuId){
			for(var i=0;i<data.length;i++){
				if(data[i].id == menuId){
					menu.items = data[i];
					break;
				}
			}
			return data[i];
		}
		return menu;
	}]);
});