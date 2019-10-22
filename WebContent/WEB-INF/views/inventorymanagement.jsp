<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.Product" import="java.util.List" %>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="Products"/>
    </jsp:include>
    <script>
    function validateForm() {
    	  var x = document.forms["updateInvetoryForm"]["number"].value;
    	  if (x < 0 or x > 100) {
    	    alert("Count must cannot be less than or over 100");
    	    return false;
    	  }
    	}
    </script>
    <body class="bg-light">
        <jsp:include page="_nav.jsp"></jsp:include>

        <main class="container mt-3">
            <div class="d-flex">
                <h1 class="mr-auto">Inventory</h1>
            </div>
            <table class="table">
                <tbody>
                    <% for (Product product : (List<Product>)request.getAttribute("inventorymanagement")) { %>
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
                            <form name="updateInvetoryForm" onsubmit="return validateForm()" action="inventory" method="POST">
                            	<input type="number" id ="count" name="count" min="0" max="100">   
                            </td>
                        </tr>
                    <% } %>
                    <tr>
                    <td colspan="3">
                    <input type="submit" class="btn btn-primary" value="Update">
                    </form>
                    </td>
                    </tr>
                </tbody>
            </table>
        </main>
        <jsp:include page="_footer.jsp"></jsp:include>
    </body>
</html>
