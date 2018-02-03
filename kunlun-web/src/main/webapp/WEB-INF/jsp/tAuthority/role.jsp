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
<form name="form1" method="post" action="../tRoleAuthority/saveAuthority.hs">
<tr>
	<td class="comm_td_title">
		&nbsp;<c:out value="${authority.name}"/>--角色管理
		&nbsp;&nbsp;<input type="button" value="&nbsp;保&nbsp;存&nbsp;" onclick="saves()"/>
		<input type="hidden" name="authorityId" value="${authority.id}"/>
	</td>
</tr>
<tr>
	<td class="comm_td"></td>
</tr>
<c:forEach var="role" items="${roleList}" varStatus="ctIndex">
	<c:if test="${ctIndex.count % 2 == 0}">
		<tr class="comm_td_1"> 
	</c:if>
	<c:if test="${ctIndex.count % 2 != 0}">
		<tr class="comm_td"> 
	</c:if>
	<c:set var="b" value="${false}"/>
	<c:forEach var="ra" items="${roleAuthorityList}">
		<c:if test="${ra.roleId==role.id}">
			<c:set var="b" value="${true}"/>
		</c:if>
	</c:forEach>
			<td>&nbsp;<input type="checkbox" name="roleId" value="${role.id}" <c:if test="${b}">checked</c:if>/>&nbsp;<c:out value="${role.name}"/></td>
	</tr>
</c:forEach>
</form>
</table>
</body>
</html>
