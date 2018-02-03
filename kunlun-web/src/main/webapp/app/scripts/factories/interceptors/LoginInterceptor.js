define(['app'],function(app){
	app.factory('loginInterceptor', ['$q','$injector', function($q,$injector) {
	    var requestInterceptor = {
	        request: function(config) {
	            var deferred = $q.defer();
	            var promiseLogin = deferred.promise;
	            
	            var $http = $injector.get("$http");
	            $http.post('tUser/isLogin.hs').success(function(data){
					if(!eval(data))
						location = "login.hs";
					deferred.resolve(config);
				});
	            
	            var promiseReq = promiseLogin.then(function(){
	            	return $http(config);
	            });
	            
	            console.log("=============*****");
	            return promiseReq;
	        }
	    };

	    return requestInterceptor;
	}]);
});