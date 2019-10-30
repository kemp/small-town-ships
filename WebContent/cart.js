(function(window, document) {
    'use strict';
	
	var cartEl = document.getElementById('cart');
	var checkoutButton, cartTotalEl, cartTotalPluralEl;
	var items = {}, itemNames = {};
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
	
	function isCartVisible() {
		return cartEl;
	}
	
	// update the total of the cart
	function updateItemTotal() {
		if (isCartVisible()) {
			cartTotalEl.innerText = getItemTotal();
		}		
	}
	
	function formatAsCurrency(amount) {
		return amount.toLocaleString("en-US", {style: "currency", currency: "USD", minimumFractionDigits: 2});
	}
	
	function getItemTotal() {
		var total = 0;

		for (var key in items) {
		    if (items.hasOwnProperty(key))
                total += items[key].price;
		}
		
		return formatAsCurrency(total);
	}
	
	// Save the cart items to storage (persist on page reloads)
	function saveItems() {
		storage.setItem('items', JSON.stringify(items));
	}
	
	// Load the cart items from storage (persist on page reloads)
	function loadItems() {
		items = JSON.parse(storage.getItem('items')) || {};
	}
	
	function isCartEmpty() {
		return Object.keys(items).length === 0;
	}
	
	// Proceed to the checkout
	function checkout() {
		if (isCartEmpty()) {
			alert("Please add an item to the cart to continue.");
			return;
		}
		
		let location = new URL('./checkout', window.location);

		window.location.href = location;
	}
	
	// public: Load checkout products
	function loadCheckoutProducts(checkoutTable, failedCheckout, checkoutInput) {
		let checkoutEl = document.getElementById(checkoutTable);
		let failedCheckoutEl = document.getElementById(failedCheckout);
		let checkoutInputEl = document.getElementById(checkoutInput);
		
		checkoutInputEl.value = '';
		
		for (const productId in items) {
		    if (items.hasOwnProperty(productId)) {
				let productName = itemNames[productId];
				let productQty = items[productId].quantity;
				let productPrice = items[productId].price;

				let html = `
				<td>${productName}</td>
				<td>${productQty}</td>
				<td>${formatAsCurrency(productPrice)}</td>
				<td><button class="btn btn-danger" onclick="window.removeItem(${productId}); location.reload()">&times;</button></td>
			`;

				let node = document.createElement('tr');
				node.innerHTML = html;

				checkoutEl.appendChild(node);

				// Update checkout input
				// Format: id,qty,;id2,qty2;...
				checkoutInputEl.value += productId + ',' + productQty + ';';
			}
		}
		
		if (isCartEmpty()) {
			failedCheckoutEl.innerText = 'Please add an item to the cart to continue.';
		} else {
			failedCheckoutEl.remove();
		}
	}
	
	// public: load product names
	function loadProductNames(names) {
		itemNames = JSON.parse(names);
	}
	
	// Public: add an item to cart given ID and quantity
	function addToCart(id, quantity, price) {
		
		if (!items[id]) 
			items[id] = { "quantity": 0, "price": 0 };
		
		var qty = parseInt(quantity) || 0;
		
		items[id].quantity += qty;
		items[id].price += price * qty;
				
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
	
	// Public: remove item from cart
	function removeItem(id) {
		delete items[id];
		
		saveItems();
	}
	
	// Public: get quantity added to cart of a product
	function getCartQuantityForProduct(productId) {
		if (! items[productId]) return 0;
		
		return items[productId].quantity || 0;
	}
	
	if (isCartVisible()) {
		initCart();
	}
	
	loadItems();
	updateItemTotal();

	// Add public functions
	window.addToCart = addToCart;
	window.clearCart = clearCart;
	window.getCartQuantityForProduct = getCartQuantityForProduct;
	window.removeItem = removeItem;
	window.getItemTotal = getItemTotal;
	
	// Add public functions related to checkout
	window.loadCheckoutProducts = loadCheckoutProducts;
	window.loadProductNames = loadProductNames;


	
})(window, document);