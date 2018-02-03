<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色表</title>
</head>
<body>
<table class="comm_table" cellspacing="0">
<form name="form1" method="post" action="delRequestmap.hs">
<tr>
	<td colspan="3" class="comm_td_title">
		&nbsp;<c:out value="${authority.name}"/>--请求资源管理
		<input type="hidden" name="authorityId" value="${authority.id}"/>
	</td>
</tr>
<tr>
	<td class="comm_td" colspan="3"></td>
</tr>
<c:forEach var="requestmap" items="${requestmapList}" varStatus="ctIndex">
	<c:if test="${ctIndex.count % 2 == 0}">
		<tr class="comm_td_1"> 
	</c:if>
	<c:if test="${ctIndex.count % 2 != 0}">
		<tr class="comm_td"> 
	</c:if>
			<td align="center"><input type="checkbox" name="requestmapId" value="${requestmap.id}"/></td>
			<td>&nbsp;<c:out value="${requestmap.url}"/></td>
			<td>&nbsp;<c:out value="${requestmap.description}"/></td>
	</tr>
</c:forEach>
<tr class="comm_td">
	<td align="center"><input type="checkbox" onclick="selectAll('requestmapId',this.checked)"/></td>
	<td colspan="2" class="comm_td">&nbsp;全选/取消全选&nbsp;&nbsp;<input type="button" value="&nbsp;删&nbsp;除&nbsp;" onclick="deleteAll('form1','requestmapId')"/></td>
</tr>
</form>
</table>
</body>
</html>
