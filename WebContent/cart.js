(function(window, document) {
	
	var cartEl = document.getElementById('cart');
	var checkoutButton, cartTotalEl, cartTotalPluralEl;
	var items = {};
	var storage = window.sessionStorage;
	
	// Create the cart, if the element exists
	function initCart() {
		var cartHTML = `
			<div class="btn-group">
				<button id="checkout-button" type="button" class="btn btn-primary">
					Proceed to checkout (<span id="cart-total">0.00</span>)
				</button>
				<button type="button" class="btn btn-primary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
				    <span class="sr-only">Toggle Dropdown</span>
				</button>
				<div class="dropdown-menu dropdown-menu-right">
				    <button class="dropdown-item" onclick="window.clearCart()">Empty cart</button>
				</div>
			</div>
		`;
		
		cartEl.innerHTML = cartHTML;
		
		checkoutButton = document.getElementById('checkout-button');
		cartTotalEl = document.getElementById('cart-total');
		cartTotalPluralEl = document.getElementById('cart-total-plural');
		
		checkoutButton.addEventListener('click', checkout);
	}
	
	// update the total of the cart
	function updateItemTotal() {
		cartTotalEl.innerText = getItemTotal();
	}
	
	function getItemTotal() {
		var total = 0;
		
		for (var key in items) {
			total += items[key]["price"];
		}
		
		return total.toLocaleString("en-US", {style: "currency", currency: "USD", minimumFractionDigits: 2});
	}
	
	// Save the cart items to storage (persist on page reloads)
	function saveItems() {
		storage.setItem('items', JSON.stringify(items));
	}
	
	// Load the cart items from storage (persist on page reloads)
	function loadItems() {
		items = JSON.parse(storage.getItem('items')) || {};
	}
	
	// Checkout!
	function checkout() {
		if (Object.keys(items).length === 0) {
			alert("Cart is empty!");
			return;
		}
		
		alert("Thank you for shopping with Small Town Ships.\nYour total is: " + getItemTotal());
	}
	
	// Public: add an item to cart given ID and quantity
	function addToCart(id, quantity, price) {
		
		if (!items[id]) 
			items[id] = { "quantity": 0, "price": 0 };
		
		var qty = parseInt(quantity) || 0;
		
		items[id]["quantity"] += qty;
		items[id]["price"] += price * qty;
				
		updateItemTotal();
		
		saveItems();
	}
	
	// Public: clear the cart
	function clearCart() {
		items = {};
		
		storage.removeItem('items');
		
		if (window.clearQuantityInCart) {
			window.clearQuantityInCart();
		}
		
		updateItemTotal();
	}
	
	// Public: get quantity added to cart of a product
	function getCartQuantityForProduct(productId) {
		if (! items[productId]) return 0;
		
		return items[productId]["quantity"] || 0;
	}
	
	if (cartEl) {
		loadItems();
		initCart();
		updateItemTotal();
		
		// Add public functions
		window.addToCart = addToCart;
		window.clearCart = clearCart;
		window.getCartQuantityForProduct = getCartQuantityForProduct;
	}

	
})(window, document);