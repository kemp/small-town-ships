<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
<link rel="stylesheet" href="register.css">
<title>Small Town Ships</title>
</head>
<body>
<form action="<%= request.getContextPath() %>/Register" method="POST">
  <div class="form-group">
  	<h1>Register</h1>
    <label for="FirstName">First Name:</label>
    <input type="text" class="form-control" id="FirstName" name="FirstName" required="required" >
    <label for="LastName">Last Name:</label>
    <input type="text" class="form-control" id="LastName" name="LastName" required="required">
    <label for="UserName">User Name:</label>
    <input type="text" class="form-control" id="UserName" name="UserName" required="required">
    <label for="inputEmail">Email</label>
    <input type="email" class="form-control" id="inputEmail" name="inputEmail" placeholder="Email" required="required">
    <label for="inputPassword">Password</label>
    <input type="password" class="form-control" id="inputPassword" name="inputPassword" placeholder="Password" required="required" 	>
  </div>
  <button type="submit" class="btn btn-primary">Register</button>
</form>
</body>
</html>