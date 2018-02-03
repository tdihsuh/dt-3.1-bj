<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../common/common.jsp"%>
<!docType html>
<html>
<head>
	<title>登录-- 招商银行安全大数据平台</title>
	<style>
		#login_pannel{
			margin-left:15px;
		}
		.login_pannel_txt{
			border-left:1px solid #C2C2C2;
			border-right:1px solid #C2C2C2;
			font-size:12px;
			height:40px;
			padding-top:20px;
			padding-left:21px;
		}
		.login_pannel_form{
			border-left:1px solid #C2C2C2;
			border-right:1px solid #C2C2C2;
			font-size:12px;
			padding-left:20px;
			padding-right:20px;
		}
	</style>
</head>
<body>
<%@ include file="common/head.jsp"%>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="hs_info_body">
<tr>
	<td colspan="5" height="50"></td>
</tr>
<tr>
	<td width="10%"></td>
	<td width="30%" valign="top"><table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td style="font-size:24px;height:60px;" class="txt-color-red"> 招商银行安全大数据平台</td>
	</tr>
	<tr>
		<td style="height:50px;">安全、稳定、快速、准确安全日志分析平台</td>
	</tr>
	<tr>
		<td style="height:100px;" valign="bottom"><div class="login-app-icons">
			<span href="javascript:void(0);" class="btn btn-danger btn-sm">使用手册</span>
			<span href="javascript:void(0);" class="btn btn-danger btn-sm">FAQ</span>
		</div></td>
	</tr>
	</table></td>
	<td width="20%" valign="top"><img src="images/login/iphoneview.png" class="pull-right display-image" style="width: 210px"></td>
	<td width="30%" valign="top"><table width="100%" cellpadding="0" cellspacing="0" border="0" id="login_pannel">
	<form action="<c:out value="${pageContext.request.contextPath}"/>/j_spring_security_check" method="post">
	<tr>
		<td class="hs_title" style="height:60px;padding-left:20px;">登&nbsp;&nbsp;录</td>
	</tr>
	<tr>
		<td class="login_pannel_txt">帐号</td>
	</tr>
	<tr>
		<td class="login_pannel_form">
			<div class="input-group">
				<span class="input-group-addon"><i class="fa fa-user"></i></span>
				<input class="form-control" type="text" name="j_username" placeholder="请输入您的系统账号">
			</div>
		</td>
	</tr>
	<tr>
		<td class="login_pannel_txt">密码</td>
	</tr>
	<tr>
		<td class="login_pannel_form">
			<div class="input-group">
				<span class="input-group-addon"><i class="fa fa-key"></i></span>
				<input class="form-control" type="password" name="j_password" placeholder="请输入您的密码">
			</div>
		</td>
	</tr>
	<tr>
		<td class="login_pannel_txt">验证码</td>
	</tr>
	<tr>
		<td class="login_pannel_form"><table width="100%" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td width="60%"><input type='text' name='j_captcha' class="form-control" size='5' /></td>
			<td width="40%"><img id="captchaImg" src="<c:url value="/jcaptcha.jpg"/>" width="120" height="40" align="absmiddle"/></td>
		</tr>
		</table></td>
	</tr>
	<tr>
		<td class="hs_title" align="right" style="height:60px;padding-right:20px;"><button type="submit" class="btn btn-primary">
			登&nbsp;录
		</button></td>
	</tr>
	</form>
	</table></td>
	<td width="10%"></td>
</tr>
</table>
<%@ include file="common/foot.jsp"%>
</body>
</html>
