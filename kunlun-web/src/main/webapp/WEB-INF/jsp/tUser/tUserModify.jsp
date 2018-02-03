<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户表</title>
<script type="text/javascript">
function saves(){
	if(confirm("确定要保存?")){
		tUserForm.submit();
	}
}
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;
		<c:choose>
			<c:when test="${tUser == null || tUser.id == null}">新增</c:when>
			<c:otherwise>编辑</c:otherwise>
		</c:choose>
		用户表
	</td>
</tr>
	<form name="tUserForm" method="post" action="save.hs">
		
		<tr>
			<td class="comm_td">&nbsp;帐号:</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tUser == null}">
						&nbsp;<input type="text" name="userId" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="userId" value="<c:out value="${tUser.userId}"/>" size="30" disabled/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="id" value="<c:out value="${tUser.id}"/>"/>
			</td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;昵称:</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tUser == null}">
						&nbsp;<input type="text" name="nickName" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="nickName" value="<c:out value="${tUser.nickName}"/>" size="30"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:choose>
			<c:when test="${tUser == null}">
				<tr>
					<td class="comm_td">&nbsp;密码：</td>
					<td class="comm_td">&nbsp;<input type="text" name="pw" size="30"/></td>
				</tr>
			</c:when>
			<c:otherwise>
				
			</c:otherwise>
		</c:choose>
		<tr>
			<td class="comm_td" valign="top">&nbsp;选择角色:</td>
			<td class="comm_td"><table class="comm_table_no_line" cellspacing="0">
				<c:forEach var="role" items="${roleList}">
					<c:set var="b" value="${false}"/>
					<c:forEach var="ur" items="${userRoleList}">
						<c:if test="${ur.roleId == role.id}"><c:set var="b" value="${true}"/></c:if>
					</c:forEach>
					<tr>
						<td class="comm_td_2">&nbsp;<input type="checkbox" name="roleIds" value="${role.id}" <c:if test="${b}">checked</c:if>/><c:out value="${role.name}"/>（<c:out value="${role.description}"/>）</td>
					</tr>
				</c:forEach>
			</table></td>
		</tr>
		
	</form>
	<tr>
		<td colspan="2" class="comm_td" align="center"><input type="button" value="&nbsp;保&nbsp;存&nbsp;" onclick="saves()"/></td>
	</tr>
</table>
</body>
</html>

