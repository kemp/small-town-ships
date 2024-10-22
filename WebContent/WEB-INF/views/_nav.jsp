<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.User" %>
<%
    User user = User.loggedInUser(request.getSession());
%>

<nav class="navbar navbar-expand-md navbar-light bg-white shadow-sm">
    <div class="container">
        <a class="navbar-brand" href="/small-town-ships">
            <img src="logo.svg" style="height: 3rem;" alt="" />
            Small Town Ships
        </a>
        
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <!-- Left Side Of Navbar -->
            <ul class="navbar-nav mr-auto">
                <% if (user != null) { %>
                	<li class="nav-item"><a class="nav-link" href="./products">Products</a></li>

                    <% if (user.isAdmin()) { %>
                        <li class="nav-item"><a class="nav-link" href="./inventory">Inventory</a></li>
                    <% } %>
                <% } %>
            </ul>
            
            <!-- Right Side Of Navbar -->
            <ul class="navbar-nav ml-auto">
                <!-- Authentication Links -->
                <% if (user == null) { %>
                    <li class="nav-item"><a class="nav-link" href="./index.jsp">Login</a></li>
                    <li class="nav-item"><a class="nav-link" href="./register.jsp">Register</a></li>
                <% } else { %>
                    <li class="nav-item"><a class="nav-link" href="./logout.jsp">Log Out (<%= user.getDisplayName() %>)</a></li>
                <% } %>
            </ul>
        </div>
    </div>
</nav>