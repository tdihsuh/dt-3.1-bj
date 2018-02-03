define(['app'],function(app){
	app.factory('Aliases',['$http','$q','hs_es','$filter',function($http,$q,hs_es,$filter){
		return {
			getIndices:function(ranges){
				var indices = "";
				for(var i in ranges){
					if(i > 0)
						indices += ",";
					indices += hs_es.log_index_name+$filter('date')(ranges[i]._d,"yyyyMMdd");
				}
				
				var deferred = $q.defer();
				$http.get(hs_es.host_name + "/" + indices  + "/_aliases?ignore_missing=true").success(function(data){
					var str = "";
					var n = 0;
					for(var i in data){
						if(n++ > 0)
							str += ",";
						str += i;
					}
					deferred.resolve(str);
				}).error(function(msg){
					deferred.reject(msg);
				});
				return deferred.promise;
			}
		}
	}]);
});