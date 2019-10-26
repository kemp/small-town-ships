<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    import="me.smalltownships.LoginHandler" %>
<% (new LoginHandler()).logout(); %>
<!doctype html>
<html lang="en">
    <jsp:include page="WEB-INF/views/_head.jsp">
        <jsp:param name="title" value="You have been logged out"/>
    </jsp:include>
    
    <body class="bg-light">
    
        <jsp:include page="WEB-INF/views/_nav.jsp"></jsp:include>

        <main class="container mt-3">
            <div>
                <h1 class="display-4">You have successfully logged out.</h1>
                
                <p>
                    <a href="./" class="btn btn-primary">Log In</a>
                </p>
            </div>
        </main>
        
        <jsp:include page="WEB-INF/views/_footer.jsp"></jsp:include>
    </body>
</html>
