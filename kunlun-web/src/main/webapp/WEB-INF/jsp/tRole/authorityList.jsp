<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色表</title>
<script type="text/javascript">
function saves(){
	if(confirm("确定要保存？")){
		form1.submit();
	}
}
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<form name="form1" method="post" action="../tRoleAuthority/saveRole.hs">
<tr>
	<td class="comm_td_title">
		&nbsp;<c:out value="${role.description}"/>--分配权限
		&nbsp;&nbsp;<input type="button" value="&nbsp;保&nbsp;存&nbsp;" onclick="saves()"/>
		<input type="hidden" name="roleId" value="${role.id}"/>
	</td>
</tr>
<tr>
	<td class="comm_td"></td>
</tr>
<c:forEach var="tAuthority" items="${authorityList}" varStatus="ctIndex">
	<c:if test="${ctIndex.count % 2 == 0}">
		<tr class="comm_td_1"> 
	</c:if>
	<c:if test="${ctIndex.count % 2 != 0}">
		<tr class="comm_td"> 
	</c:if>
	<c:set var="b" value="${false}"/>
	<c:forEach var="authority" items="${list}">
		<c:if test="${authority.id==tAuthority.id}">
			<c:set var="b" value="${true}"/>
		</c:if>
	</c:forEach>
			<td>&nbsp;<input type="checkbox" name="authorityId" value="${tAuthority.id}" <c:if test="${b}">checked</c:if>/>&nbsp;<c:out value="${tAuthority.description}"/></td>
	</tr>
</c:forEach>
</form>
</table>
</body>
</html>
