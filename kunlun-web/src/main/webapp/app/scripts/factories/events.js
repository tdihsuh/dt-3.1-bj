define(['app','scripts/factories/pagination'],function(app,Pagination){
	app.factory('EventItems',['$http','$q','Pagination',function($http,$q,Pagination){
		var event = {};
		event.pageNum = 0;
		event.currentPage = 0;
		event.offset = 0;
		event.pageSize = 10;
		event.query = function(url,para,currentPage){
			if(currentPage >= 1){
				event.currentPage = currentPage;
			}else{
				event.currentPage = 1;
			}
			var deferred = $q.defer();
			var num = 0;
			if(currentPage > 0){
				event.offset = (currentPage-1)*event.pageSize;
			}
			
			para.from = event.offset;
			para.size = event.pageSize;
			$http.post(url,para).success(function(data){
				var pagination = Pagination(currentPage,data.hits.total);
				event.pageNum = pagination.pageNum;
				
				event.items = [];
				var hits = data.hits.hits;
				for(var i=0;i<hits.length;i++){
					event.items[i] = {
						_id:hits[i]._id,
						_index:hits[i]._index,
						_type:hits[i]._type
					}
					event.items[i]["_source"] = hits[i]._source;
				}
				deferred.resolve(event);
			});
			return deferred.promise;
		};
		return event;
	}]);
});