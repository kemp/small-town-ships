<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.LoginHandler" %>
<nav class="navbar navbar-expand-md navbar-light bg-white shadow-sm">
    <div class="container">
        <a class="navbar-brand" href="/small-town-ships">
            Small Town Ships
        </a>
        
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <!-- Left Side Of Navbar -->
            <ul class="navbar-nav mr-auto">
                <li class="nav-item"><a class="nav-link" href="./products">Products</a></li>
            </ul>
            
            <!-- Right Side Of Navbar -->
            <ul class="navbar-nav ml-auto">
                <!-- Authentication Links -->
                <% 
                LoginHandler lh = new LoginHandler(); 
                if (! lh.isLoggedIn()) { 
                %>
                    <li class="nav-item"><a class="nav-link" href="./index.jsp">Login</a></li>
                    <li class="nav-item"><a class="nav-link" href="./register.jsp">Register</a></li>
                <% 
                } else { 
                %>
                    <li class="nav-item"><a class="nav-link" href="./logout.jsp">Log Out</a></li>
                <% 
                }
                try {
                    lh.close();   
                } catch (Exception e) {
                    e.printStackTrace();   
                }
                %>
            </ul>
        </div>
    </div>
</nav>