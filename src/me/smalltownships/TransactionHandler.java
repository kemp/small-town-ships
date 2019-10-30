package me.smalltownships;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TransactionHandler extends InteractsWithSQL {
	
	public static void createAdminTransaction(Map<Product, Integer> products) {
		// Loop through the list of products and use procedure "New_IMS_Transaction"
		for (Map.Entry<Product, Integer> productSet : products.entrySet()) {
			sqlHandler.callProcedure("New_IMS_Transaction(?,?,?)", 3, new String[] {
				"1",
				Integer.toString(productSet.getKey().getId()),
				Integer.toString(productSet.getValue()) // Use positive to represent gained inventory
			});
		}
		
		// Balance the inventory afterward
		sqlHandler.callProcedure("Balance_Inventory()");
	}

	public static void createTransaction(Map<Product, Integer> products, String deliveryAddress, String creditCardNumber, String creditCardExpiration) {
		try {
			// Get the ID of the next transaction
			ResultSet rs = sqlHandler.callProcedure("Next_Trans_ID()");
			
			int transactionId;
			
			if (rs.next()) {
				transactionId = rs.getInt("b");
			} else {
				throw new SQLException("Next_Trans_ID() SQL procedure returned invalid data.");
			}
			
			// Username of the currently logged in user
			String user = (new LoginHandler()).loggedInUsername();
			if (user == null) {
				throw new RuntimeException("No logged-in user");
			}
			
			// Create a user transaction using "New_Transaction" stored procedure
			sqlHandler.callProcedure("New_Transaction(?,?,?,?,?,?)", 6, new String[] {
				Integer.toString(transactionId),
				user,
				Long.toString(computeGrandTotal(products)),
				creditCardNumber,
				creditCardExpiration,
				deliveryAddress
			});
			
			// Loop through the list of products and use procedure "New_IMS_Transaction"
			for (Map.Entry<Product, Integer> productSet : products.entrySet()) {
				sqlHandler.callProcedure("New_IMS_Transaction(?,?,?)", 3, new String[] {
					Integer.toString(transactionId),
					Integer.toString(productSet.getKey().getId()),
					Integer.toString(productSet.getValue() * -1) // Use inverse to represent lost inventory
				});
			}
			
			// Balance the inventory afterward
			sqlHandler.callProcedure("Balance_Inventory()");
		} catch (SQLException e) {
			throw new RuntimeException("Could not process transaction", e);
		}
	}
	
	/**
	 * Helper method to compute the grand total of a transaction
	 * 
	 * @param products list
	 * @return grand total
	 */
	private static long computeGrandTotal(Map<Product, Integer> products) {
		long total = 0;
		
		for (Map.Entry<Product, Integer> productSet : products.entrySet()) {
			total += productSet.getKey().getPrice() * productSet.getValue();
		}
		
		return total;
	}
}
