<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
<link rel="stylesheet" href="test.css">
<title>Small Town Ships</title>
</head>
<body>
	<form action="Controller.java" method="POST">
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