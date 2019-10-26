<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.Product" import="java.util.List" %>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="Checkout"/>
    </jsp:include>
    
    <body class="bg-light" onload="loadCheckoutProducts('checkout-products', 'checkout-products-failed', 'products-form-input')">
    
        <jsp:include page="_nav.jsp"></jsp:include>

        <main class="container mt-3 mb-5">
            <div class="d-flex">
                <h1 class="mr-auto">Checkout</h1>
            </div>
            
                
            <table class="table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Total</th>
                        <th>Remove</th>
                    </tr>
                </thead>
                <tbody id="checkout-products">
                </tbody>
            </table>
            
            <div class="alert alert-danger" id="checkout-products-failed">Failed loading products for checkout.</div>
            
            
            <div><strong>Grand Total: </strong> <span id="item-total">$--.--</span></div>
            
            
            <hr />
            
            <h2>Customer details</h2>
            
            <form method="POST">
            
                <!-- TODO: CSRF Token -->
            
                <input type="hidden" name="products" id="products-form-input" value="">
            
                <div class="form-group">
                    <label for="address">Full Delivery address:</label>
                    <input 
                        class="form-control" 
                        type="text" 
                        id="address" 
                        name="address" 
                        placeholder="123 Main St., Atlanta, GA, 30041"
                        required
                    />
                </div>
                
                <div class="form-group">
                    <label for="cc-number">Credit Card Number:</label>
                    <input 
                        class="form-control" 
                        type="text" 
                        id="cc-number" 
                        name="cc-number" 
                        placeholder="1234 1234 1234 1234" 
                        maxlength="16" 
                        pattern="[0-9]{16}" 
                        required
                    />
                </div>   
                
                <div class="form-group">
                    <label for="cc-exp">Credit Card Expiration:</label>
                    <input 
                        class="form-control" 
                        type="text" 
                        id="cc-exp" 
                        name="cc-exp" 
                        placeholder="01/2020" 
                        pattern="[0-9]{2}/[0-9]{4}" 
                        required
                    />
                </div> 
                
                
                <div class="d-flex">
                    <input type="submit" class="btn btn-lg btn-success ml-auto" value="Submit Order">
                </div>
            
            </form>
            
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
    
        <script src="cart.js"></script>
        
        <script>
        	loadProductNames('<%= request.getAttribute("productNames") %>');
        	document.getElementById('item-total').innerText = getItemTotal();
        </script>
    </body>
</html>
