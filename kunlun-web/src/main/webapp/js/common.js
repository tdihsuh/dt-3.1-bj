$(function(){
	$("#comm_selectAll").click(function(){
		$("input[name=ids]").each(function(){
			$(this).attr("checked",!!$("#comm_selectAll").attr("checked"));
		});
	});
});

function selectAll(idName,checked){
	$("input[name="+idName+"]").each(function(){
			$(this).attr("checked",checked);
	});
}

function deleteAll(formName,idName){
	var b = false;
	$("input[name="+idName+"]").each(function(){
			if(!!$(this).attr("checked")){
				b = true;
			}
	});
	if(b){
		if(confirm("确定要删除选中的记录？")){
			eval("document."+formName).submit();
		}
	}else{
		alert("请先选择要删除的记录！");
		return false;
	}
}