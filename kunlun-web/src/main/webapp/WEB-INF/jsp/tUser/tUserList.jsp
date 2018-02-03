<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户表</title>
<script type="text/javascript">
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="7" class="comm_td_title">&nbsp;用户表查询</td>
</tr>
				<tr>
			<td width="10%" class="comm_td_1">&nbsp;帐号：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="userId"/></td>
			<td width="10%" class="comm_td_1">&nbsp;昵称：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="nickName"/></td>
			<td width="10%" class="comm_td_1">&nbsp;注册日期：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="createDate"/></td>
					</tr>
				<tr>
			<td width="10%" class="comm_td_1">&nbsp;最后登录日期：</td>
			<td width="13%" class="comm_td">&nbsp;<input type="text" name="lastLoginDate"/></td>
			<td class="comm_td_1">&nbsp;</td>
			<td class="comm_td">&nbsp;</td>
			<td class="comm_td_1">&nbsp;</td>
			<td class="comm_td">&nbsp;</td>
		</tr>
<tr>
	<td colspan="7" align="center" class="comm_td"><input type="button" name="searchs" value="&nbsp;查&nbsp;询&nbsp;"/></td>
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
				<td class="comm_td_title">帐号</td>
				<td class="comm_td_title">电子邮箱</td>
				<td class="comm_td_title">昵称</td>
				<td class="comm_td_title">注册日期</td>
				<td class="comm_td_title">最后登录日期</td>
		<td class="comm_td_title">操作</td>
	</tr>
	<c:forEach var="tUser" items="${tUserList}" varStatus="ctIndex">
		<c:if test="${ctIndex.count % 2 == 0}">
			<tr> 
		</c:if>
		<c:if test="${ctIndex.count % 2 != 0}">
			<tr> 
		</c:if>
			<td align="center" class="comm_td"><input type="checkbox" name="ids" value="<c:out value="${tUser.id}"/>"/></td>
					<td class="comm_td">&nbsp;
							<c:out value="${tUser.userId}"/>
					</td>
					<td class="comm_td">&nbsp;
							<c:out value="${tUser.email}"/>
					</td>
					<td class="comm_td">&nbsp;
							<c:out value="${tUser.nickName}"/>
					</td>
					<td class="comm_td">&nbsp;
							<c:out value="${tUser.createDate}"/>
					</td>
					<td class="comm_td">&nbsp;
							<c:out value="${tUser.lastLoginDate}"/>
					</td>
			<td align="center" class="comm_td">
				<a href="modify.hs?id=<c:out value="${tUser.id}"/>">编辑</a>&nbsp;&nbsp;
				<a href="detail.hs?id=<c:out value="${tUser.id}"/>">查看</a>&nbsp;&nbsp;
				<a href="javascript:" onclick="comm_del('<c:out value="${tUser.id}"/>')">删除</a>
			</td>
		</tr>
	</c:forEach>
	<tr>
		<td align="center" class="comm_td"><input type="checkbox" id="comm_selectAll"/></td>
		<td colspan="6" class="comm_td">&nbsp;全选/取消全选&nbsp;&nbsp;<input type="button" id="comm_dels" value="&nbsp;删&nbsp;除&nbsp;"/></td>
	</tr>
</table>
</body>
</html>
