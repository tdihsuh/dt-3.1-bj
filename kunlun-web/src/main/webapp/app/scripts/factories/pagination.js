define(['app'],function(app){
	app.factory('Pagination',function(){
		return function(currentPage,recordNum,size){
			var pagination = {
				pageSize:10,
				offset:0,
				limit:0,
				pageNum:0,
				init:function(){
					if(size) this.pageSize = size;
					if(currentPage > 0){
						this.offset = (currentPage-1)*this.pageSize;
					}
					this.limit = this.pageSize;
					this.pageNum = Math.floor((recordNum-1)/this.pageSize)+1;
				}
			};
			pagination.init();
			return pagination;
		};
	});
});