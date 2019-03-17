<%--
  Created by IntelliJ IDEA.
  User: cynicism
  Date: 2019/3/17
  Time: 18:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>cas client demo2</title>
</head>
<body>
欢迎来到品优购2 <%=request.getRemoteUser()%>
<a href="http://localhost:9100/cas/logout?service=http://www.baidu.com">退出登录</a>

</body>
</html>
