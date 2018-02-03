define(['app'],function(app){
	app.factory('Role',['$http','$q',function($http,$q){
		function Role(data){
			if(data){
				this.setData(data);
			}
		};
		Role.prototype = {
			setData : function(data){
				angular.extend(this,data);
			},
			list: [],
			query:function(){
	            var scope = this;
	            var deferred = $q.defer();
				$http.post('tRole/list.hs').success(function(data){
                    data.model.roleList.forEach(function(r) {
                    	scope.list.push(r);
                    });
                    deferred.resolve(scope.list);
				}).error(function() {
                    deferred.reject();
                });
	            return deferred.promise;
			}
		};
		return Role;
	}]);
});