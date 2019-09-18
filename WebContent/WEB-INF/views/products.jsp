<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.Product" import="java.util.List" %>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="Products"/>
    </jsp:include>
    
    <body class="bg-light">
    
        <jsp:include page="_nav.jsp"></jsp:include>

        <main class="container mt-3">
            <h1>Products</h1>
            	
            <div class="card-columns">
                <% for (Product product : (List<Product>)request.getAttribute("products")) { %>
                    <div class="card mx-auto mb-2">
                        <a href="./product?id=<%= product.getId() %>">
                            <img src="<%= product.getImage() %>" class="card-img-top" alt="<%= product.getName() %>">
                        </a>
                        <div class="card-body">
                            <h5 class="card-title"><%= product.getName() %></h5>
                            <p class="card-text">Price: <%= product.getFormattedPrice() %></p>
                            <a href="./product?id=<%= product.getId() %>" class="btn btn-info">Open</a>
                        </div>
                    </div>
                <% } %>
            </div>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
    </body>
</html>
