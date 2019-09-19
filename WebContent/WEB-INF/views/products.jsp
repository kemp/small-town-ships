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
            <div class="d-flex">
                <h1 class="mr-auto">Products</h1>
                
                <div id="cart"></div>
            </div>
            
            	
            <table class="table">
                <tbody>
                    <% for (Product product : (List<Product>)request.getAttribute("products")) { %>
                        <tr>
                            <td>
                                <a href="./product?id=<%= product.getId() %>">
                                    <img src="<%= product.getImage() %>" class="card-img-top" alt="<%= product.getName() %>">
                                </a>
                            </td>
                            <td>
                                <h2><%= product.getName() %></h2>
                                <%= product.getDescription() %>
                                <p>Price: <%= product.getFormattedPrice() %></p>
                            </td>
                            <td style="vertical-align: middle">
                                <a href="./product?id=<%= product.getId() %>" class="btn btn-info">Open</a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
    
        <script src="cart.js"></script>
    </body>
</html>
