<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.LoginHandler" %>
<%

LoginHandler loginHandler = new LoginHandler();

if (loginHandler.isLoggedIn()) {
    response.sendRedirect("products");
}

try {
    loginHandler.close();
} catch (Exception e) {
    e.printStackTrace();
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
    	    	<input type="text" class="form-control" id="username" aria-describedby="emailHelp" placeholder="Enter email" name="username">
    		</div>
    		<div class="form-group">
    	    	<label for="password">Password</label>
    	    	<input type="password" class="form-control" id="password" placeholder="Password" name="password">
    		</div>
    		<button type="submit" class="btn btn-primary">Login</button>
    		<br>
    		<p>Don't have an account? <a href="./register.jsp">Sign up</a></p>
    	</form>
    </body>
</html>