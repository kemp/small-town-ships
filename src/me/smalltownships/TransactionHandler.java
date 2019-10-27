package me.smalltownships;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TransactionHandler extends InteractsWithSQL {

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
			
			// Create a user transaction using "New_Transaction" stored procedure
			sqlHandler.callProcedure("New_Transaction(?,?,?,?)", 4, new String[] {
				Integer.toString(transactionId),
				user,
				Long.toString(computeGrandTotal(products)),
				creditCardNumber
			});
			
			// Loop through the list of products and use procedure "New_IMS_Transaction"
			for (Map.Entry<Product, Integer> productSet : products.entrySet()) {
				sqlHandler.callProcedure("New_IMS_Transaction(?,?,?)", 3, new String[] {
					Integer.toString(transactionId),
					Integer.toString(productSet.getKey().getId()),
					Integer.toString(productSet.getValue() * -1) // Use inverse to represent lost inventory
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
