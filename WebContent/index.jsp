  <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="/WEB-INF/views/_head.jsp"></jsp:include>
        <link rel="stylesheet" href="index.css">
    </head>
    <body>
    	<form action="login" method="POST">
    		<h1>Login</h1>
    	 	<div class="form-group">
    	    	<label for="username">Username</label>
    	    	<input type="text" class="form-control" id="username" aria-describedby="emailHelp" placeholder="Enter email">
    		</div>
    		<div class="form-group">
    	    	<label for="password">Password</label>
    	    	<input type="password" class="form-control" id="password" placeholder="Password">
    		</div>
    		<button type="submit" class="btn btn-primary">Submit</button>
    	</form>
    </body>
</html>