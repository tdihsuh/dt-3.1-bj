<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<c:set var="contextPath" scope="request"><c:out value="${pageContext.request.contextPath}"/></c:set>

<script type="text/javascript" src="<c:out value="${contextPath}"/>/vendor/jquery/jquery-2.1.1.min.js" ></script>
<script type="text/javascript" src="<c:out value="${contextPath}"/>/js/common.js" ></script>

<link href="<c:out value="${contextPath}"/>/css/style.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="screen" href="<c:out value="${contextPath}"/>/vendor/font-awesome-4.2.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" media="screen" href="<c:out value="${contextPath}"/>/vendor/bootstrap-3.2.0-dist/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" media="screen" href="<c:out value="${contextPath}"/>/css/jquery-ui-1.10.4.min.css">
<link rel="stylesheet" type="text/css" media="screen" href="<c:out value="${contextPath}"/>/css/tooltipster.css">
<link rel="stylesheet" type="text/css" media="screen" href="<c:out value="${contextPath}"/>/css/tooltipster-shadow.css">