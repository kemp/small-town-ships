package me.smalltownships;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Collections;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Generate a web page to verify an email address
 * 
 * @author Joshua Fehrenbach
 */
public class EmailVerifier {
	
	static class VerificationPage implements Comparable<VerificationPage> {
		final String name;
		final Date date;
		
		VerificationPage(String name, String date) {
			this.name = name;
			this.date = Date.valueOf(date);
		}
		
		@Override
		public int compareTo(VerificationPage o) {
			return name.compareTo(o.name);
		}
	}
	
	private static final Encoder base64Encode;
	private static final SecureRandom rand;
	private static final NavigableSet<VerificationPage> verificationPages;
	
	private static final String secureRandAlg;
	
	private static final AtomicLong uniqueSeed = new AtomicLong(1);
	
	private static long getUniqueSeed() {
		for (;;) {
			long current = uniqueSeed.get();
			long next = current * 181783497276652981L + System.nanoTime();
			for (int i = 0; i < 256; i++) {
				next = current * 181783497276652981L + System.nanoTime();
			}
			if (uniqueSeed.compareAndSet(current, next)) {
				return next;
			}
		}
	}
	
	static {
		SecureRandom tmp = null;
		try {
			// Attempt to get a strong RNG
			tmp = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e1) {
			// No strong RNG found, try to find a secure RNG
			String[] algs = { "PKCS11", "SHA1PRNG", "NativePRNG", "NativePRNGBlocking",
					"NativePRNGNonBlocking" };
			for (String alg : algs) {
				try {
					tmp = SecureRandom.getInstance(alg);
					break;
				} catch (NoSuchAlgorithmException e2) { }
			}
			if (tmp == null) {
				// No secure RNG found, so default to java.util.Random
				tmp = new SecureRandom();
			}
		}
		tmp.setSeed(tmp.generateSeed(256));
		tmp.setSeed(tmp.nextLong() ^ System.nanoTime() ^ tmp.nextLong());
		rand = tmp;
		secureRandAlg = rand.getAlgorithm();
		verificationPages = Collections.synchronizedNavigableSet(new TreeSet<>());
		base64Encode = Base64.getUrlEncoder().withoutPadding();
	}

	public static SecureRandom getSecureRandom() {
		SecureRandom r;
		try {
			r = SecureRandom.getInstance(secureRandAlg);
		} catch (NoSuchAlgorithmException e) {
			r = new SecureRandom();
		}
		r.setSeed(r.generateSeed(256));
		r.setSeed(r.nextLong() ^ System.nanoTime() ^ r.nextLong());
		r.setSeed(getUniqueSeed());
		return r;
	}
	
	/**
	 * Move the account with the indicated username from unverifiedaccounts to
	 * verifiedaccounts.
	 * 
	 * @param user The username of the account that has been verified
	 * @return True if the account was moved to the verifiedaccounts table, false otherwise
	 */
	public static boolean verifyAccount(String user) {
		String first, last, pass, email, sql;
		ResultSet rs;
		try (MySQLHandler sqlHandler = new MySQLHandler()) {
			sql = "get firstname, lastname, password, email from "
					+ "smalltownships.unverifiedaccounts where username='" + user + "';";
			rs = sqlHandler.performStatement(sql);
			if (rs.getRow() == 0) {		// No entry found
				rs.close();
				return false;
			}
			first = rs.getString(1);
			last = rs.getString(2);
			pass = rs.getString(3);
			email = rs.getString(4);
			rs.close();
			sql = "delete from smalltownships.unverifiedaccounts where"
					+ " username='" + user + "';";
//			sqlHandler.performUpdate(sql);
			sql = "insert into smalltownships.verifiedaccounts values "
					+ "("+first+", "+last+", "+user+", "+pass+", "+email+", 0);";
//			sqlHandler.performUpdate(sql);
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create a new email verification page with a randomly generated URL, and email
	 * a verification link the given email address.
	 * 
	 * @param email The email address to verify
	 */
	public static void createVerificationPage(String email, String username, String date)
			throws IOException {
		VerificationPage page;
		String pageName;
		// Ensure that the address created is unique
		synchronized (rand) {	// Synchronize to prevent creating one page for two emails
			byte[] randBytes = new byte[16];	// 128-bit random integer
			do {
				rand.nextBytes(randBytes);
				// Ensure the encoded string is in UTF-8 format
				pageName = new String(base64Encode.encode(randBytes), StandardCharsets.UTF_8);
				page = new VerificationPage(pageName, date);
			} while (!verificationPages.add(page));
			// pageName now holds a unique, randomly generated, verification page name
		}
		File fpage = null;
		try {
			// Create the JSP web page
			// TODO: Find out if this path is correct
			fpage = new File("/" + pageName + ".html");
			fpage.createNewFile();
			try (FileWriter out = new FileWriter(fpage)) {
				out.write("<!doctype html>\n");
				out.write("<html lang=\"en\">\n");
				out.write("<head>\n");
				out.write("  <!-- Required meta tags -->\n");
				out.write("    <meta charset=\"utf-8\">\n");
				out.write("    <meta name=\"viewport\" content=\"width=device-width, "
						+ "initial-scale=1, shrink-to-fit=no\">\n");
				out.write("    <!-- Bootstrap CSS -->\n");
				out.write("    <link rel=\"stylesheet\" href=\"test.css\">\n");
				out.write("    <title>Small Town Battleships</title>\n");
				out.write("  </head>\n");
				out.write("  <body>\n");
				out.write("    <div class=\"login\">\n");
//				  		<form action="/action_page.php" class="container">
//				   			<label for="username">Username</label>
//				   			<br>
//				    		<input type="text" placeholder="Enter Email" name="username" required>
//				    		<br>
//				    		<label for="psw">Password</label>
//				    		<br>
//				    		<input type="password" placeholder="Enter Password" name="psw" required>
//				    		<br>
//				    		<button type="submit" class="btn">Login</button>
//				  		</form>
//				  	</div>
//				  </body>
//				</html>
			}
		} catch (IOException e) {
			if (fpage != null) {
				fpage.delete();
			}
			verificationPages.remove(page);
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
