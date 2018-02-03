var hsMinWindow = function(obj){
	if($(obj).html().indexOf("fa-minus") == -1){
		$(obj).parent().parent().nextAll().css("display","block");
		$(obj).html("<i class=\"fa fa-minus\"></i>");
	}else{
		$(obj).parent().parent().nextAll().css("display","none");
		$(obj).html("<i class=\"fa fa-plus\"></i>");
	}
};
var hsFullWindow = function(obj){
	if($(obj).html().indexOf("fa-resize-full") == -1){
		console.log($("body").html());
		var parentObj = $(obj).parent().parent().parent();
		parentObj.css("position","");
		$(obj).html("<i class=\"fa fa-resize-full\"></i>");
	}else{
		var parentObj = $(obj).parent().parent().parent();
		$(obj).html("<i class=\"fa fa-resize-small\"></i>");
		var str = "<div class='hs_full'>"+parentObj.html()+"</div>";
		$("body").html($("body").html()+str);
	}
}
Date.prototype.format = function(format){
	/*
	* eg:format="YYYY-MM-dd hh:mm:ss";
	*/
	var o = {
		"M+" :  this.getMonth()+1,  //month
		"d+" :  this.getDate(),     //day
		"H+" :  this.getHours(),    //hour
		"m+" :  this.getMinutes(),  //minute
		"s+" :  this.getSeconds(), //second
		"q+" :  Math.floor((this.getMonth()+3)/3),  //quarter
		"S"  :  this.getMilliseconds() //millisecond
	}

	if(/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
	}

	for(var k in o) {
		if(new RegExp("("+ k +")").test(format)) {
		  format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
		}
	}
	return format;
};
String.prototype.trim = function() {
	return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}