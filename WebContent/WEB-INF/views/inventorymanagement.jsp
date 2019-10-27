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

        <div class="container mt-3">
        <form name="updateInvetoryForm" onsubmit="return validateForm()" action="inventory" method="POST">
            <table class="table">
            	
                <tbody>
                <tr>
                <th>
                <h1>Image</h1>
                </th>
                <th>
                <h1>Name</h1>
                </th>
                <th>
                <h1>Description</h1>
                </th>
                <th>
                <h1>Pricing</h1>
                </th>
                <th>
                <h1>Quantity</h1>
                </th>
                </tr>
                    <% for (Product product : (List<Product>)request.getAttribute("inventorymanagement")) { %>
                        <tr>
                        	<form name="updateInvetoryForm" onsubmit="return validateForm()" action="inventory" method="POST">
                            <td >
                                <a href="./product?id=<%= product.getId() %>">
                                    <img src="<%= product.getImage() %>" class="card-img-top" alt="<%= product.getName() %>" width="100">
                                </a>
                            </td>
                            <td>                    
                            	<input class="form-control form-control-sm" type="text" id="name" name="name" required="required" value="<%= product.getName() %>">  
                            </td>
                            <td>
                                <textarea rows="6" cols="50" id="description" name="description" required="required"><%= product.getDescription() %></textarea>
                                <p>Price: <%= product.getFormattedPrice() %></p>
                            </td>
                            <td style="vertical-align: middle">
                            	<div class="col-20">
                            		<input class="form-control form-control-sm" type="number" id ="price" name="price" min="1" max="1000000000000000" placeholder="<%= product.getFormattedPrice() %>" value="<%= product.getStringFormattedPrice() %>">
                            	</div>
                            </td>
                            <td style="vertical-align: middle">
                            	<input class="form-control form-control-sm" type="number" id ="quantity" name="quantity" min="0" max="100000" value="<%= product.getQuantity() %>">	
                            </td>
                            <td style="vertical-align: middle">
                            	<input type="submit" class="btn btn-primary" value="Update">
                            </td>
                            </form>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <jsp:include page="_footer.jsp"></jsp:include>
    </body>
</html>
