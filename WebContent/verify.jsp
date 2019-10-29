<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="me.smalltownships.EmailVerifier" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <jsp:include page="/WEB-INF/views/_head.jsp" />
    
  </head>
  <body>
    <jsp:include page="/WEB-INF/views/_nav.jsp" />
    <main class="container mt-3">
      <div>
        <% if (EmailVerifier.verifyAccount(request.getParameter("id"))) { %>
          <h1 class="display-4">Your account has been verified.</h1>
          <p>
            <a href="./" class="btn btn-primary">Log In</a>
          </p>
        <% } else { %>
          <h1 class="display-4">Your account could not be verified.</h1>
        <% } %>
      </div>
    </main>
  </body>
</html>