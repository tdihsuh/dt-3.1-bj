define(['app'],function(app){
	app.factory('User',['$http','$q',function($http,$q){
		function User(data){
			if(data){
				this.setData(data);
			}
		};
		User.prototype = {
			setData : function(data){
				angular.extend(this,data);
			},
			prop: [],
			query:function(){
	            var scope = this;
	            var deferred = $q.defer();
				$http.post('tUser/current.hs').success(function(data){
					scope.prop = data;
                    deferred.resolve(scope.prop);
				}).error(function() {
                    deferred.reject();
                });
	            return deferred.promise;
			}
		};
		return User;
	}]);
});