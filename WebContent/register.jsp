<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="me.smalltownships.LoginHandler"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    
    <link rel="stylesheet" href="register.css">
    <title>Create New Account</title>
  </head>
  <body>
<%!
String fieldValue(String param) {
	if (param == null || param.isEmpty()) {
		return "";
	} else {
		return "value=\""+param+"\"";
	}
}
%>
    <div class="register">
      <%
      String err = request.getParameter("err");
      if (err != null && !err.isEmpty()) {
    	  if (err.equals("1")) {
    		  out.println("<p style=\"color:red\">Username already exists</p>");
    	  } else if (err.equals("2")) {
    		  out.println("<p style=\"color:red\">Email address already in use</p>");
    	  } else if (err.equals("3")) {
    		  out.println("<p style=\"color:red\">Password does not match</p>");
    	  }
      }
      %>
      <form action="<%= request.getContextPath() %>/Register" class="container" method="post">
        <label for="firstname">First Name</label>
        <br>
        <input type="text" placeholder="First Name" name="firstname"
            <%= fieldValue(request.getParameter("fn")) %> required>
        <br>
        <label for="lastname">Last Name</label>
        <br>
        <input type="text" placeholder="Last Name" name="lastname"
            <%= fieldValue(request.getParameter("ln")) %> required>
        <br>
        <!-- TODO: Add a "User Name already taken" function -->
        <label for="username">User Name</label>
        <br>
        <input type="text" placeholder="User Name" name="username"
            <%= fieldValue(request.getParameter("un")) %> required>
        <br>
        <!-- TODO: Add a check for password and confirm password mismatch -->
        <label for="psw">Password</label>
        <br>
        <input type="password" placeholder="Enter Password" name="psw" required>
        <br>
        <label for="confirm_psw">Confirm Password</label>
        <br>
        <input type="password" placeholder="Confirm Password" name="confirm_psw" required>
        <br>
        <label for="email">Email Address</label>
        <br>
        <input type="text" placeholder="Email Address" name="email"
            <%= fieldValue(request.getParameter("ea")) %> required>
        <br>
        <button type="submit" class="btn">Create Account</button>
      </form>
    </div>
  </body>
</html>