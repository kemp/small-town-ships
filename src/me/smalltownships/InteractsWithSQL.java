package me.smalltownships;

public abstract class InteractsWithSQL {
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find JDBC libraries", e);
		}
		
		sqlHandler = new MySQLHandler();
	}
	
	protected static MySQLHandler sqlHandler;

}
