package me.smalltownships;

import java.io.File;
import java.sql.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MySQLHandler implements AutoCloseable {
	
	static {
		// Load JDBC SSL settings from XML file
		String path = System.getProperty("catalina.home") +
				File.separator + "webapps" + File.separator;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(path + "SmallTownShipsConfig.xml"));
			Element root = doc.getDocumentElement();
			root.normalize();
			// extract truststore and keystore passwords
			Element db = (Element) root.getElementsByTagName("database").item(0);
			String ts = db.getElementsByTagName("truststore").item(0).getTextContent();
			String ks = db.getElementsByTagName("keystore").item(0).getTextContent();
			System.setProperty("javax.net.ssl.trustStorePassword", ts);
			System.setProperty("javax.net.ssl.keyStorePassword", ks);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Set truststore and keystore path
		path += "SSL" + File.separator;
		System.setProperty("javax.net.ssl.trustStore", path + "truststore");
		System.setProperty("javax.net.ssl.keyStore", path + "keystore");
	}
	
	Connection con;
	
	public MySQLHandler() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document config = builder.parse(new File(System.getProperty("catalina.home") + File.separator + "webapps" + File.separator + "SmallTownShipsConfig.xml"));
			Element root = config.getDocumentElement();
			root.normalize();
			Element db = (Element) root.getElementsByTagName("database").item(0);
			String dbpswd = db.getElementsByTagName("password").item(0).getNodeValue();
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/smalltownships?allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", dbpswd);
			if(con == null) {
				System.out.println("Not connected to database");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 *  Designed to be called by LoginHandler / Registration page
	 *  Returns a result set that can be manipulated
	 *  
	 *  TODO Secure this via 'protected' in package with classes that need access to it
	 *  
	 *  @param SQL string to be executed
	 *  @return Result set of table that was queried
	 */
//	public ResultSet queryTable(String sql) {
//		ResultSet rs = null;
//		try {
//			Statement stmt = con.createStatement();
//			return stmt.executeQuery(sql);
//			} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return rs;
//	}
	public ResultSet callProcedure(String procedureName) {
		ResultSet rs = null;
		String sql = "{CALL "+procedureName+"}";
		try {
			CallableStatement stmt = con.prepareCall(sql);
			rs = stmt.executeQuery();
		} catch (SQLException e) {e.printStackTrace();}
		return rs;
	}
	public ResultSet callProcedure(String procedureName, int arg, String[] args) {
		ResultSet rs = null;
		String sql = "{ CALL "+procedureName+" }";
		//System.out.println(sql);
		try {
			CallableStatement stmt = con.prepareCall(sql);
			for (int i = 1; i <= arg; i++) {
				stmt.setString(i, args[i-1]);
			}
			rs = stmt.executeQuery();
		} catch (SQLException e) {e.printStackTrace();}
		return rs;
	}
	/**
	 *  Function for issuing data manipulation statements to database
	 *  
	 *  @param String SQL statement that should return nothing
	 *  @return True if statement executed, false if an exception was thrown
	 */
//	public boolean updateTable(String sql) {
//		try {
//			Statement stmt = con.createStatement();
//			stmt.executeUpdate(sql);
//			return true;
//			} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
	
	/**
	 * Utility method, allowing for conversion of verifiedAccounts table to String
	 * 
	 * @param ResultSet from the verifiedAccounts table to be converted to string
	 * @return String representation of table
	 */
	public String verifiedToString(ResultSet rs) {
		String s = "";
		try {
			int columnCount = rs.getMetaData().getColumnCount();
			while(rs.next()){
				s = s.concat(rs.getRow()+": ");
				for (int i = 1; i <= columnCount; i++) {
					String value = rs.getString(i);
					s = s.concat("["+value+"] ");
				}
				s = s.concat("\n");
			}
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
		// Close the database connection
		// https://stackoverflow.com/a/249149
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
	}
}
