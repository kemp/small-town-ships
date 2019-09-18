package me.smalltownships;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.sql.Date;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Generate a web page to verify an email address
 * 
 * @author Joshua Fehrenbach
 */
public class EmailVerifier {
	
	private static final Encoder base64Encode;
	private static final SecureRandom rand;
	private static final NavigableMap<String,Date> verificationPages;
	
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
		verificationPages = Collections.synchronizedNavigableMap(new TreeMap<String,Date>());
		base64Encode = Base64.getUrlEncoder().withoutPadding();
	}
	
	public static void verifyAccount(String username) {
		MySQLHandler sqlHandler = new MySQLHandler();
		String sql = "get * from smalltownships.unverifiedaccounts where"
				+ " username='" + username + "';";
		String sqlDeleteUnverified = "delete from smalltownships.unverifiedaccounts where"
				+ " username='" + username + "';";
		
	}
	
	/**
	 * Create a new email verification page with a randomly generated URL, and email
	 * a verification link the given email address.
	 * 
	 * @param email The email address to verify
	 */
	public static void createVerificationPage(String email, String username)
			throws IOException {
		String pageName;
		Date pageDate;
		// Ensure that the address created is unique
		synchronized (rand) {	// Synchronize to prevent creating one page for two emails
			byte[] randBytes = new byte[16];	// 128-bit random integer
			do {
				rand.nextBytes(randBytes);
				// Ensure the encoded string is in UTF-8 format
				pageName = new String(base64Encode.encode(randBytes), StandardCharsets.UTF_8);
				pageDate = Date.valueOf(LocalDate.now());
			} while (verificationPages.putIfAbsent(pageName, pageDate) != null);
			// pageName now holds a unique, randomly generated, verification page name
		}
		File page = null;
		try {
			// Create the JSP web page
			// TODO: Find out if this path is correct
			page = new File("WebContent/" + pageName + ".jsp");
			page.createNewFile();
			try (FileWriter out = new FileWriter(page)) {
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
//					<div class="login">
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
			if (page != null) {
				page.delete();
			}
			verificationPages.remove(pageName);
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
