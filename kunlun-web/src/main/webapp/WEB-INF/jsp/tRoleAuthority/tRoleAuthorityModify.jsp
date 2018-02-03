<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<script type="text/javascript">
function saves(){
	if(confirm("确定要保存?")){
		tRoleAuthorityForm.submit();
	}
}
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;
		<c:choose>
			<c:when test="${tRoleAuthority == null || tRoleAuthority.roleId == null}">新增</c:when>
			<c:otherwise>编辑</c:otherwise>
		</c:choose>
		
	</td>
</tr>
	<form name="tRoleAuthorityForm" method="post" action="save.hs">
		
		<tr>
			<td class="comm_td">&nbsp;:</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tRoleAuthority == null}">
						&nbsp;<input type="text" name="authorityId" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="authorityId" value="<c:out value="${tRoleAuthority.authorityId}"/>" size="30"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		
	</form>
	<tr>
		<td colspan="2" class="comm_td" align="center"><input type="button" value="&nbsp;保&nbsp;存&nbsp;" onclick="saves()"/></td>
	</tr>
</table>
</body>
</html>

