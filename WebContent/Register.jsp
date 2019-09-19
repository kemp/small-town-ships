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
<form>
  <div class="form-group">
  	<h1>Register</h1>
    <label for="FirstName">First Name:</label>
    <input type="text" class="form-control" id="FirstName">
    <label for="LastName">Last Name:</label>
    <input type="text" class="form-control" id="LastName">
    <label for="inputEmail4">Email</label>
    <input type="email" class="form-control" id="inputEmail4" placeholder="Email">
    <label for="inputPassword4">Password</label>
    <input type="password" class="form-control" id="inputPassword4" placeholder="Password">
  </div>
  <button type="submit" class="btn btn-primary">Register</button>
</form>
</body>
</html>