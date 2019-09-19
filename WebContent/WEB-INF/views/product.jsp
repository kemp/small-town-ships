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
            <div class="d-flex">
                <h1 class="mr-auto">${ product.name }</h1>
                
                <div id="cart"></div>
            </div>
            
            <div class="card mx-auto mb-2">
                <img src="${ product.image }" class="card-img-top" alt="${ product.name }">
                <div class="card-body">
                    <p class="card-text">
                        <div>${ product.description }</div>
                        <div>
                            <h2>Specifications</h2>
                            ${ product.specifications }
                        </div>
                        <br><strong>Unit price:</strong> ${ product.getFormattedPrice() }
                    </p>
                    <div id="item-alert" class="alert alert-success my-2" style="display: none;">Your cart has been updated!</div>
                    <form id="item-form" onsubmit="pushCart(event, ${ product.id }, ${ product.price })">
                        <div class="form-group">
                            <label for="quantity">Quantity (<span id="qty-available"></span> available): </label>
                            <select class="form-control" id="quantity-dropdown" name="quantity">
                            </select>
                        </div>
                        <input type="submit" class="btn btn-success" value="Add to Cart" />
                    </form>
                </div>
            </div>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>

        <script src="cart.js"></script>
        
        <script>
        	(function(window, document) {
            	window.available = 0;
            	window.timer = null;
            	
    			window.pushCart = function (event, id, price) {
    				event.preventDefault();
    				
    				var formData = new FormData(event.target);
    				
    				var qty = formData.get('quantity');
    								
    				window.addToCart(id, qty, price);
    				
    				document.getElementById('item-alert').style.display = 'block';
    				
    				clearTimeout(timer);
    				
    				window.timer = setTimeout(function() {
        				document.getElementById('item-alert').style.display = 'none';
    				}, 4000);
    				
    				setQuantityAvailable(getQuantityAvailable() - qty);
    			}
    			
    			window.setQuantityAvailable = function (available) {
    				window.available = available;
    				
    				// Update label
    				document.getElementById('qty-available').innerText = available;				
    				
    				// Get dropdown
    				var dropdown = document.getElementById('quantity-dropdown');
    				
    				// Remove all elements from the dropdown
    				while (dropdown.hasChildNodes()) {
    					dropdown.removeChild(dropdown.lastChild);
    				}
    				
    				// Re-add all options to dropdown
    				for (var i = 1; i <= available; i++) {
    					var el = document.createElement('option');
    					el.value = i;
    					el.innerText = i;
    					
    					dropdown.appendChild(el);
    				}
    			}
    			
    			window.getQuantityAvailable = function () {
    				return available;
    			}
    			
    			window.clearQuantityInCart = function () {
    				setQuantityAvailable(${ product.quantity });
    			}
    			
    			setQuantityAvailable(
    				${ product.quantity } - window.getCartQuantityForProduct(${ product.id })
    			);
        	})(window, document);
        </script> 
    </body>
</html>
