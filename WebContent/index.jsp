  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<jsp::include page="/WEB-INF/views/_head.jsp">
</jsp::include>
<link rel="stylesheet" href="index.css">
<title>Small Town Ships</title>
</head>
<body>
	<form action="LoginServlet" method="POST">
		<h1>Login</h1>
	 	<div class="form-group">
	    	<label for="username">Username</label>
	    	<input type="email" class="form-control" id="username" aria-describedby="emailHelp" placeholder="Enter email">
		</div>
		<div class="form-group">
	    	<label for="password">Password</label>
	    	<input type="password" class="form-control" id="password" placeholder="Password">
		</div>
		<button type="submit" class="btn btn-primary">Submit</button>
	</form>
</body>
</html>