<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../common/common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>请求资源表</title>
<script type="text/javascript">
function saves(){
	if(confirm("确定要保存?")){
		tRequestmapForm.submit();
	}
}
</script>
</head>
<body>
<table class="comm_table" cellspacing="0">
<tr>
	<td colspan="2" class="comm_td_title">&nbsp;
		<c:choose>
			<c:when test="${tRequestmap == null || tRequestmap.id == null}">新增</c:when>
			<c:otherwise>编辑</c:otherwise>
		</c:choose>
		请求资源表
	</td>
</tr>
	<form name="tRequestmapForm" method="post" action="save.hs">
		<tr>
			<td class="comm_td">&nbsp;请求地址：</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tRequestmap == null}">
						&nbsp;<input type="text" name="url" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="url" value="<c:out value="${tRequestmap.url}"/>" size="30"/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="id" value="<c:out value="${tRequestmap.id}"/>"/>
			</td>
		</tr>
		<tr>
			<td class="comm_td">&nbsp;请求描述：</td>
			<td class="comm_td">
				<c:choose>
					<c:when test="${tRequestmap == null}">
						&nbsp;<input type="text" name="description" size="30"/>
					</c:when>
					<c:otherwise>
						&nbsp;<input type="text" name="description" value="<c:out value="${tRequestmap.description}"/>" size="30"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td class="comm_td" valign="top">&nbsp;分配权限：</td>
			<td class="comm_td"><table class="comm_table_no_line" cellspacing="0">
			<c:forEach var="authority" items="${authorityList}">
				<c:set var="b" value="${false}"/>
				<c:forEach var="ar" items="${arList}">
					<c:if test="${ar.authorityId == authority.id}"><c:set var="b" value="${true}"/></c:if>
				</c:forEach>
				<tr>
					<td class="comm_td">&nbsp;<input type="checkbox" name="authorityIds" value="${authority.id}" <c:if test="${b}">checked</c:if>/><c:out value="${authority.name}"/>（<c:out value="${authority.description}"/>）</td>
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

