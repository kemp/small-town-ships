<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <jsp:include page="/WEB-INF/views/_head.jsp" />
    <jsp:include page="/WEB-INF/views/_nav.jsp" />
    <%
    if (Boolean.TRUE.equals(request.getAttribute("me.smalltownships.Register.err"))) {
    %>
    <style>
    #err {
      color:red;
      font-weight:bold;
    }
    </style>
    <% } %>
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
    <%
    String msg = (String)request.getAttribute("me.smalltownships.Register.fnerrmsg");
    if (msg != null) {
    %>
    <br> <span id="err"><%= msg %></span>
    <% } %>
    <label for="LastName" class="col-form-label col-form-label-lg">Last Name:</label>
    <input type="text" class="form-control form-control-lg" id="LastName" name="LastName" placeholder="Last Name" required="required">
    <%
    msg = (String)request.getAttribute("me.smalltownships.Register.lnerrmsg");
    if (msg != null) {
    %>
    <br> <span id="err"><%= msg %></span>
    <% } %>
    <label for="UserName" class="col-form-label col-form-label-lg">User Name:</label>
    <input type="text" class="form-control form-control-lg" id="UserName" name="UserName" placeholder="Username"  required="required">
    <%
    msg = (String)request.getAttribute("me.smalltownships.Register.unerrmsg");
    if (msg != null) {
    %>
    <br> <span id="err"><%= msg %></span>
    <% } %>
    <label for="inputEmail" class="col-form-label col-form-label-lg">Email</label>
    <input type="email" class="form-control form-control-lg" id="inputEmail" name="inputEmail" placeholder="Email" required="required">
    <%
    msg = (String)request.getAttribute("me.smalltownships.Register.emerrmsg");
    if (msg != null) {
    %>
    <br> <span id="err"><%= msg %></span>
    <% } %>
    <label for="inputPassword" class="col-form-label col-form-label-lg">Password</label>
    <input type="password" class="form-control form-control-lg" id="inputPassword" name="inputPassword" placeholder="Password" required="required">
    <%
    msg = (String)request.getAttribute("me.smalltownships.Register.pwerrmsg");
    if (msg != null) {
    %>
    <br> <span id="err"><%= msg %></span>
    <% } %>
    <br>
    <button type="submit" class="btn btn-primary btn-lg btn-block">Register</button>
  	<%
  	msg = (String)request.getAttribute("me.smalltownships.Register.errmsg");
    if (msg != null) {
  	%>
    <br> <span id="err"><%= msg %></span>
  	<% } %>
  </div>
  </div>
  </form>
  </body>
</html>