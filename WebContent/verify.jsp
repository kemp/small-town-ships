<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="me.smalltownships.EmailVerifier" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <jsp:include page="/WEB-INF/views/_head.jsp"></jsp:include>
    <link rel="stylesheet" href="register.css">
  </head>
  <body>
    <div class="verified">
      <%
        if (EmailVerifier.verifyAccount(request.getParameter("id"))) {
        	out.println("Your account has been verified.");
        } else {
        	out.println("Your account could not be verified.");
        }
      %>
    </div>
  </body>
</html>