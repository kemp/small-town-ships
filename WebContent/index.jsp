<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.LoginHandler" %>
<%
// Only allow unauthenticated users.
if ((new LoginHandler()).isLoggedIn()) {
    response.sendRedirect("products");
}
%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="/WEB-INF/views/_head.jsp"></jsp:include>
        <link rel="stylesheet" href="index.css">
    </head>
    <body>
        <jsp:include page="/WEB-INF/views/_nav.jsp"></jsp:include>
    
    	<form action="login" method="POST">
    		<h1>Login</h1>
    	 	<div class="form-group">
    	    	<label for="username">Username</label>
    	    	<input type="text" class="form-control" id="username" placeholder="Enter username" name="username">
    		</div>
    		<div class="form-group">
    	    	<label for="password">Password</label>
    	    	<input type="password" class="form-control" id="password" placeholder="Password" name="password">
    		</div>
			<%
			if (request.getAttribute("me.smalltownships.login.loginerr") != null) {
			%>
			<span style="color:red;font-weight:bold;">Incorrect username or password</span>
			<% } %>
    		<button type="submit" class="btn btn-primary">Login</button>
    		<br>
    		<p>Don't have an account? <a href="./register.jsp">Sign up</a></p>
    	</form>
    </body>
</html>