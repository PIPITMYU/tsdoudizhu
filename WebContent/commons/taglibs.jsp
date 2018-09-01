<%@page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
%>

<c:set var="random" value="<%=Math.random() %>"></c:set>
<c:set var="basePath" value="<%=basePath %>"></c:set>
<c:set var="contextPath" value="<%=path %>"></c:set>
<c:set var="jsPath" value="${contextPath}/js"/>
<c:set var="cssPath" value="${contextPath}/css"/>
<c:set var="imgPath" value="${contextPath}/img"/>
<script type="text/javascript">
	ctxPath = '<%=basePath%>';
</script>