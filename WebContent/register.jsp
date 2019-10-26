<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <jsp:include page="/WEB-INF/views/_head.jsp" />
    <jsp:include page="/WEB-INF/views/_nav.jsp" />
  </head>
  <body class="bg-light">
  <br>
  <br>
  <br>
  <form action="Register" method="POST">
  <div class="row justify-content-center">
  <div class="form-group">
  	<h1 class="display-1" >Register</h1>
    <label for="FirstName" class="col-form-label col-form-label-lg">First Name:</label>
    <input type="text" class="form-control form-control-lg" id="FirstName" name="FirstName" placeholder="First Name" required="required" >
    <label for="LastName" class="col-form-label col-form-label-lg">Last Name:</label>
    <input type="text" class="form-control form-control-lg" id="LastName" name="LastName" placeholder="Last Name" required="required">
    <label for="UserName" class="col-form-label col-form-label-lg">User Name:</label>
    <input type="text" class="form-control form-control-lg"" id="UserName" name="UserName" placeholder="Username"  required="required">
    <label for="inputEmail" class="col-form-label col-form-label-lg">Email</label>
    <input type="email" class="form-control form-control-lg" id="inputEmail" name="inputEmail" placeholder="Email" required="required">
    <label for="inputPassword" class="col-form-label col-form-label-lg">Password</label>
    <input type="password" class="form-control form-control-lg" id="inputPassword" name="inputPassword" placeholder="Password" required="required">
    <br>
    <button type="submit" class="btn btn-primary btn-lg btn-block">Register</button>
  </div>
  </div>

</form>
  </body>
</html>