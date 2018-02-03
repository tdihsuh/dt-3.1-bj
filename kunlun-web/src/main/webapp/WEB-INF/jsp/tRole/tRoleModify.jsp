<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色表</title>
<script type="text/javascript">
function saves(){
	if(confirm("确定要保存?")){
		tRoleForm.submit();
	}
}
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;
		<c:choose>
			<c:when test="${tRole == null || tRole.id == null}">新增</c:when>
			<c:otherwise>编辑</c:otherwise>
		</c:choose>
		角色表
	</td>
</tr>
	<form name="tRoleForm" method="post" action="save.hs">
		
		<tr>
			<td class="comm_td_1">&nbsp;角色名称：</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tRole == null}">
						&nbsp;<input type="text" name="name" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="name" value="<c:out value="${tRole.name}"/>" size="30"/>
						<input type="hidden" name="id" value="<c:out value="${tRole.id}"/>"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td class="comm_td_1">&nbsp;角色描述：</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tRole == null}">
						&nbsp;<input type="text" name="description" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="description" value="<c:out value="${tRole.description}"/>" size="30"/>
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

