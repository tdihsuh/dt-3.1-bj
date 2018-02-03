define(['app','scripts/factories/pagination'],function(app,Pagination){
	app.factory('LogItems',['$http','$q','$filter','Pagination','hs_info',function($http,$q,$filter,Pagination,hs_info){
		var log = {};
		log.logNum = 0;		//日志总数
		log.pageNum = 0;	//页数
		log.currentPage = 0;	//当前页数
		log.offset = 0;
		log.pageSize = 20;
		log.took = null;
		log.query = function(url,para,currentPage){
			if(currentPage >= 1){
				log.currentPage = currentPage;
			}else{
				log.currentPage = 1;
			}
			var deferred = $q.defer();
			var num = 0;
			if(currentPage > 0){
				log.offset = (currentPage-1)*log.pageSize;
			}
			para.from = log.offset;
			para.size = log.pageSize;
			$http.post(url,para).success(function(data){
				var pagination = Pagination(currentPage,data.hits.total,log.pageSize);
				log.pageNum = pagination.pageNum;
				
				log.items = [];
				var hits = data.hits.hits;
				log.logNum = data.hits.total;
				log.took = data.took;
				for(var i=0;i<hits.length;i++){
					log.items[i] = {
						_id:hits[i]._id,
						_index:hits[i]._index,
						_type:hits[i]._type
					};
					log.items[i]["_source"] = {};
					if(hits[i]._source){
						log.items[i]["_source"] = hits[i]._source;
					}
					log.items[i]["detail"] = (function(){
						//var pattern = /\(?(?:(http|https|ftp):\/\/)?(?:((?:[^\W\s]|\.|-|[:]{1})+)@{1})?((?:www.)?(?:[^\W\s]|\.|-)+[\.][^\W\s]{2,4}|localhost(?=\/)|\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})(?::(\d*))?([\/]?[^\s\?]*[\/]{1})*(?:\/?([^\s\n\?\[\]\{\}\#]*(?:(?=\.)){1}|[^\s\n\?\[\]\{\}\.\#]*)?([\.]{1}[^\s\?\#]*)?)?(?:\?{1}([^\s\n\#\[\]]*))?([\#][^\s\n]*)?\)?/gi;
						var pattern = /^(ftp|http|https):\/\//;
						var pattern1 = /^\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
						var pattern2 = /[\+\;\(\)]/;
						var details = "";
						var eles;
						var ele;
						for(var element in hits[i]._source){
							details += "<span>";
							ele = "";
							if(element == "@timestamp"){//日期类型不做为keywords
								ele = $filter('date')(hits[i]._source[element],"yyyy-MM-dd HH:mm:ss");
								details += ele;
							}else if(pattern1.test(hits[i]._source[element])){//ip
								details += "<span class='log_detail_comm'>"+hits[i]._source[element]+"</span>";
							}else if((pattern.test(hits[i]._source[element]) || !pattern2.test(hits[i]._source[element]))
								&& /\W/.test(hits[i]._source[element]) && /\D/.test(hits[i]._source[element])){//如果为ip地址或目录地址
								var arr = hits[i]._source[element].toString().split("?");
								
								var preUrl = arr[0];
								ele = preUrl.replace(/\w+|\:|\/\/|\.|\/|-|[\u4e00-\u9fa5]/g,function(str){
									if(/\w+|[\u4e00-\u9fa5]/.test(str))
										return "<span class='log_detail_re'>"+str+"</span>";
									else
										return "<span>"+str+"</span>";
								});
								if(arr.length > 1){
									for(var j=1;j<arr.length;j++){
										ele += "?" + arr[j].replace(/[^?=&]\*/g,function(str){
											return "<span class='log_detail_comm'>"+str+"</span>";
										});
									}
								}
									
								details += ele;
							}else{
								if(typeof hits[i]._source[element] == "string"){//如果是字符串，则按单词分隔
									var patt = /\+|\;|\/|\(|\)|\=|\:/;
									ele = hits[i]._source[element].replace(/\w+|[\u4e00-\u9fa5]/g,function(str){
										return "<span class='log_detail_comm'>"+str+"</span>";
									});
									details += ele;
								}else{
									details += "<span class='log_detail_comm'>"+hits[i]._source[element]+"</span>";
								}
							}
							details += "</span> ";
						}
						return details;
					})();
				}
				deferred.resolve(log);
			}).error(function(e){
				$("#search_popup_layers").css("display","none");
				alert(hs_info.errors);
			});;
			return deferred.promise;
		};
		return log;
	}]);
});