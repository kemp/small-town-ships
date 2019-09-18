<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.Product"%>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="${ product.name }"/>
    </jsp:include>
    
    <body class="bg-light">
        <jsp:include page="_nav.jsp"></jsp:include>
    
        <main class="container mt-3">
            <h1>${ product.name }</h1>
            
            <div class="card mx-auto mb-2">
                <img src="${ product.image }" class="card-img-top" alt="${ product.name }">
                <div class="card-body">
                    <p class="card-text">
                        <div>${ product.description }</div>
                        <br><strong>Unit price:</strong> ${ product.getFormattedPrice() }
                    </p>
                    <form action="./buy-product?id=${product.id}" method="POST">
                        <div class="form-group">
                            <label for="quantity">Quantity (${ product.quantity } available): </label>
                            <select class="form-control" id="quantity" name="quantity">
                                <% for (int i = 1; i <= ((Product)request.getAttribute("product")).getQuantity(); i++) { %>
                                    <option value="<%= i %>"><%= i %></option>
                                <% } %>
                            </select>
                        </div>
                        <p><strong>Total:</strong> <span id="total">${ product.getFormattedPrice() }</span></p>
                        <script>
                        	(function(){
                        		var quantityEl = document.getElementById('quantity');
                        		var totalEl = document.getElementById('total');
                        		
                        		function reloadTotal() {
                        			var price = ${ product.price };
                        			totalEl.innerText =	(price * quantityEl.value)
                        				.toLocaleString("en-US", {style: "currency", currency: "USD", minimumFractionDigits: 2});
                        		}
                        		
                        		document.getElementById('quantity').addEventListener('input', reloadTotal);
                        		reloadTotal();
                        	})();
                        </script>
                        <input type="submit" class="btn btn-success" value="Add to Cart" /><!-- TODO @kemp -->
                    </form>
                </div>
            </div>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
    </body>
</html>
