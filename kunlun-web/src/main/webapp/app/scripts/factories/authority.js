define(['app'],function(app){
	app.factory('Authority',['$http','$q',function($http,$q){
		function Authority(data){
			if(data){
				this.setData(data);
			}
		};
		Authority.prototype = {
			setData : function(data){
				angular.extend(this,data);
			},
			map: [],
			query:function(){
	            var scope = this;
	            var deferred = $q.defer();
				$http.post('app/data/authority.json').success(function(data){
                    scope.map = data;
                    deferred.resolve(scope.map);
				}).error(function() {
                    deferred.reject();
                });
	            return deferred.promise;
			}
		};
		return Authority;
	}]);
});