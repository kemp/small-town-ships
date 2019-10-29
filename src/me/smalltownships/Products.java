package me.smalltownships;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of products
 * 
 * @author kemp
 */
public class Products extends InteractsWithSQL {
	
	/**
	 * Fetch a list of in-stock products
	 * 
	 * @return the products
	 */
	public static List<Product> getProducts() {
    	List<Product> productsList = new ArrayList<Product>();
    	
    	ResultSet rs = sqlHandler.callProcedure("Stocked_Products");
    	
    	try {
			while (rs.next()) {
				productsList.add(new Product(
					rs.getInt("id"),
					rs.getString("name"),
					rs.getString("description"),
					rs.getString("specifications"),
					rs.getDouble("unit_price"),
					rs.getInt("quantity"),
					rs.getString("url")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace(); // TODO: Show error to user
		}
    	
    	return productsList;
	}
	
	/**
	 * Fetch a list of all products. For admin use only.
	 * 
	 * @return the products
	 */
	public static List<Product> getAllProducts() {
    	List<Product> productsList = new ArrayList<Product>();
    	
    	ResultSet rs = sqlHandler.callProcedure("All_Products");
    	
    	try {
			while (rs.next()) {
				productsList.add(new Product(
					rs.getInt("id"),
					rs.getString("name"),
					rs.getString("description"),
					rs.getString("specifications"),
					rs.getDouble("unit_price"),
					rs.getInt("quantity"),
					rs.getString("url")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace(); // TODO: Show error to user
		}
    	
    	return productsList;
	}
	
	/**
	 * Fetches the product by its given id.
	 * 
	 * @param id The permanent identifier of the product
	 * @return Product | null
	 */
	public static Product findProductByID(int id) {
		for (Product p : getProducts()) {
			if (p.getId() == id) {
				return p;
			}
		}
		
		return null;
	}
}
