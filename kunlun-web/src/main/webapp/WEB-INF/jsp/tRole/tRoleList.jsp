<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色表</title>
<script type="text/javascript">
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="6" class="comm_td_title">&nbsp;角色表查询</td>
</tr>
				<tr>
			<td width="10%" class="comm_td_1">&nbsp;角色名称：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="name"/></td>
			<td width="10%" class="comm_td_1">&nbsp;角色描述：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="description"/></td>
			<td class="comm_td_1">&nbsp;</td>
			<td class="comm_td">&nbsp;</td>
		</tr>
<tr>
	<td colspan="6" align="center" class="comm_td"><input type="button" name="searchs" value="&nbsp;查&nbsp;询&nbsp;"/></td>
</tr>
</table><br/>
<table class="comm_table_no_line" cellspacing="0">
<tr>
	<td><input type="button" value="&nbsp;新&nbsp;增&nbsp;" onclick="location='add.hs';"/></td>
</tr>
</table>
<table class="comm_table" cellspacing="0">
	<tr align="center">
		<td class="comm_td_title">&nbsp;</td>
				<td class="comm_td_title">角色名称</td>
				<td class="comm_td_title">角色描述</td>
		<td class="comm_td_title">权限管理</td>
		<td class="comm_td_title">操作</td>
	</tr>
	<c:forEach var="tRole" items="${tRoleList}" varStatus="ctIndex">
		<c:if test="${ctIndex.count % 2 == 0}">
			<tr class="comm_td"> 
		</c:if>
		<c:if test="${ctIndex.count % 2 != 0}">
			<tr class="comm_td_1"> 
		</c:if>
			<td align="center"><input type="checkbox" name="ids" value="<c:out value="${tRole.id}"/>"/></td>
					<td>&nbsp;
							<c:out value="${tRole.name}"/>
					</td>
					<td>&nbsp;
							<c:out value="${tRole.description}"/>
					</td>
			<td align="center"><a href="authority.hs?id=${tRole.id}">分配权限</a></td>
			<td align="center">
				<a href="modify.hs?id=<c:out value="${tRole.id}"/>">编辑</a>&nbsp;&nbsp;
				<a href="detail.hs?id=<c:out value="${tRole.id}"/>">查看</a>&nbsp;&nbsp;
				<a href="javascript:" onclick="comm_del('<c:out value="${tRole.id}"/>')">删除</a>
			</td>
		</tr>
	</c:forEach>
	<tr>
		<td align="center" class="comm_td"><input type="checkbox" id="comm_selectAll"/></td>
		<td colspan="4" class="comm_td">&nbsp;全选/取消全选&nbsp;&nbsp;<input type="button" id="comm_dels" value="&nbsp;删&nbsp;除&nbsp;"/></td>
	</tr>
</table>
</body>
</html>
