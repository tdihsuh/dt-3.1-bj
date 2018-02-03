<%@ page contentType="text/html; charset=utf-8" %>
<%@ page isELIgnored="false" %>
<%@ include file="../../common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>test json</title>
<script type="text/javascript">
	function queryUserList(){
		$.ajax({
			   type: "GET",
			   url: "tUser/json/list.hs",
			   contentType : "application/json",//application/xml  
		       processData : true,//contentType为xml时，值为false  
		       dataType : "json",//json--返回json数据类型；xml--返回xml  
			   success: function(msg){
			     alert( "Json Data: " + msg.model.list[0].userId );
			   }
		});
	}
</script>
</head>
<body>
	<input type="button" value="获取json数据" onclick="queryUserList()"/>
</body>
</html>