<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户表</title>
<script type="text/javascript">
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;用户表</td>
</tr>
		<tr>
			<td class="comm_td">&nbsp;帐号:</td>
			<td class="comm_td">&nbsp;<c:out value="${tUser.userId}"/></td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;电子邮箱:</td>
			<td class="comm_td">&nbsp;<c:out value="${tUser.email}"/></td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;昵称:</td>
			<td class="comm_td">&nbsp;<c:out value="${tUser.nickName}"/></td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;注册日期:</td>
			<td class="comm_td">&nbsp;<c:out value="${tUser.createDate}"/></td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;最后登录日期:</td>
			<td class="comm_td">&nbsp;<c:out value="${tUser.lastLoginDate}"/></td>
		</tr>
	<tr>
		<td colspan="2" class="comm_td" align="center"><input type="button" value="&nbsp;返&nbsp;回&nbsp;" onclick="history.go(-1)"/></td>
	</tr>
</table>
</body>
</html>

