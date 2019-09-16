package me.smalltownships;

import java.text.NumberFormat;

public class Product {
	
	private int id;
	
	private String name;
	
	private String description;
	
	private double price;
	
	private int quantity;
	
	private String image;
	
	public Product(int id, String name, String description, double price, int quantity, String image) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
		this.image = image;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
	public String getImage() {
		return this.image;
	}
	
	public String getFormattedPrice() {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		return nf.format(this.price);
	}
}
