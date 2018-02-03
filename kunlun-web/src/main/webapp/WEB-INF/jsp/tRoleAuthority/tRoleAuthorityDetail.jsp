<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<script type="text/javascript">
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;</td>
</tr>
		<tr>
			<td class="comm_td">&nbsp;:</td>
			<td class="comm_td">&nbsp;<c:out value="${tRoleAuthority.authorityId}"/></td>
		</tr>
	<tr>
		<td colspan="2" class="comm_td" align="center"><input type="button" value="&nbsp;返&nbsp;回&nbsp;" onclick="history.go(-1)"/></td>
	</tr>
</table>
</body>
</html>

