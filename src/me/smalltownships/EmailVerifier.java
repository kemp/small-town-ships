package me.smalltownships;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Collections;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.Properties;
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
		final String user;
		
		VerificationPage(String name, String date, String user) {
			this.name = name;
			this.date = Date.valueOf(date);
			this.user = user;
		}
		
		@Override
		public int compareTo(VerificationPage o) {
			return name.compareTo(o.name);
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ date.hashCode() ^ user.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof String || obj instanceof VerificationPage)) {
				return false;
			} else if (obj instanceof String) {
				return obj.equals(this.name);
			} else {
				return ((VerificationPage)obj).name.equals(this.name);
			}
		}

		@Override
		public String toString() {
			return "[" + name + "," + date + "," + user + "]";
		}
	}
	
	private static final Encoder base64Encode;
	private static final SecureRandom rand;
	private static final NavigableSet<VerificationPage> verificationPages;
	
	private static final String secureRandAlg;
	
	private static final AtomicLong uniqueSeed = new AtomicLong(1);
	
	private static long getUniqueSeed() {
		for (;;) {
			// Generate an almost completely unpredictable random seed
			// for a SecureRandom object, and also guarantee it is different from
			// the previously generated seed
			long current = uniqueSeed.get();
			long next = current * 181783497276652981L + System.nanoTime();
			next = next * 8682522807148012L + Runtime.getRuntime().freeMemory();
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

	/**
	 * Get an instance of the strongest available SecureRandom implementation
	 * 
	 * @return A SecureRandom object
	 */
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
			sql = "SELECT firstname, lastname, password, email FROM "
					+ "smalltownships.unverifiedaccounts where username='" + user + "';";
			rs = sqlHandler.queryTable(sql);
			if (rs.getRow() == 0) {		// No entry found
				rs.close();
				return false;
			}
			first = rs.getString(1);
			last = rs.getString(2);
			pass = rs.getString(3);
			email = rs.getString(4);
			rs.close();
			sql = "DELETE FROM smalltownships.unverifiedaccounts WHERE"
					+ " username='" + user + "';";
			sqlHandler.updateTable(sql);
			sql = "INSERT INTO smalltownships.verifiedaccounts VALUES "
					+ "("+first+", "+last+", "+user+", "+pass+", "+email+", 0);";
			sqlHandler.updateTable(sql);
			return true;
		} catch (SQLException e) {
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
		synchronized (verificationPages) {
			// Synchronize to prevent creating one page for two emails
			byte[] randBytes = new byte[16];	// 128-bit random integer
			do {
				rand.nextBytes(randBytes);
				// Ensure the encoded string is in UTF-8 format
				pageName = new String(base64Encode.encode(randBytes), StandardCharsets.UTF_8);
				pageName = "/" + pageName + ".jsp";
				page = new VerificationPage(pageName, date, username);
			} while (!verificationPages.add(page));		// attempt to cache page
			// Now have a unique page
		}
		File fpage = null;
		try {
			// Create the JSP web page
			// TODO: Find out if this path is correct
			fpage = new File("/" + pageName + ".jsp");
			fpage.createNewFile();
			try (FileWriter out = new FileWriter(fpage)) {
				out.write(PAGE_HEADER);
				out.write(username);
				out.write(PAGE_FOOTER);
			}
		} catch (IOException e) {
			if (fpage != null) {
				fpage.delete();
			}
			verificationPages.remove(page);
			throw e;
		}
		sendVerificationEmail(email, username, pageName);
	}
	
	private static final String PAGE_HEADER =
		"<%@ page language=\"java\" contentType=\"test/html; charset=UTF-8\"\n" +
		"  pageEncoding=\"UTF-8\" %>\n" +
		"<%@ page import=\"me.smalltownships.EmailVerifier\" %>\n" +
		"<!DOCTYPE html>\n" +
		"<html lang=\"en\">\n" +
		"<head>\n" +
		"  <!-- Required meta tags -->\n" +
		"    <meta charset=\"utf-8\">\n" +
		"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1,\n" +
		"            shrink-to-fit=no\">\n" +
		"    <!-- Bootstrap CSS -->\n" +
		"    <link rel=\"stylesheet\" href=\"register.css\">\n" +
		"    <title>Small Town Battleships</title>\n" +
		"  </head>\n" +
		"  <body>\n" +
		"    <div class=\"register\">\n" +
		"      Your Account has been Verified\n" +
		"    </div>\n" +
		"  </body>\n" +
		"<% EmailVerifier.verifyAccount(";
	
	private static final String PAGE_FOOTER = "); %>\n</html>\n";

	static {
		System.setProperty("mail.smtp.host", "smtp.gmail.com");
		System.setProperty("mail.smtp.port", "587");
		System.setProperty("mail.smtp.auth", "true");
        System.setProperty("mail.smtp.starttls.enable", "true");
	}
	
	private static class HTMLDataSource implements DataSource {
		private String html;
		
		public HTMLDataSource(String html) {
			this.html = html;
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public String getName() {
			return "HTMLDataSource";
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (html == null) {
				throw new IOException("No Input!");
			}
			return new ByteArrayInputStream(html.getBytes());
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new IOException("The handler does not handle output");
		}
	}
	
	private static final String serverEmailAccount = "???@gmail.com";
	private static final String serverEmailPassword = "password";
	
	/** TODO: find what this should actually be */
	private static final String WEB_ADDRESS = "http://localhost:8080/small-town-ships";
	
	/**
	 * Send a verification email
	 * 
	 * @param email Email address to send to
	 * @param username User's username
	 * @param path Verification page path
	 */
	public static void sendVerificationEmail(String email, String username, String path) {
		Properties p = System.getProperties();
		
		Session s = Session.getInstance(p, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(serverEmailAccount, serverEmailPassword);
			}
		});
		
		try {
			Message msg = new MimeMessage(s);
			try {
				msg.setFrom(new InternetAddress(serverEmailAccount, "Small Town Ships"));
			} catch (UnsupportedEncodingException e) {
				// Never going to happen
				throw new RuntimeException(e);
			}
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
			msg.setSubject("Small Town Ships Account Verification");
			msg.setDataHandler(new DataHandler(new HTMLDataSource(
				"<h1>Thank you " + username + " for registering with " +
				"Small Town Ships!</h1><br><h3>Please <a href=\"" +
				WEB_ADDRESS + path + "\">" +
				"click here</a> to verify your account.</h3>")));
			
			Transport.send(msg);
		} catch (MessagingException e) {
			throw new RuntimeException("Verification Message Exception", e);
		}
	}
	
	private static final Thread BACKGROUND_PAGECHECK_THREAD;
	
	private static class PageChecker implements Runnable {
		
		public void run() {
			while (true) {
				// Remove all old pages and users that took too long to verify
				synchronized (verificationPages) {
					try (MySQLHandler handler = new MySQLHandler()) {
						Date date = Date.valueOf(LocalDate.now().minusDays(2));
						ArrayList<VerificationPage> pages = new ArrayList<>();
						// Get all pages older than 2 days
						verificationPages.forEach((page) -> {
							if (date.compareTo(page.date) > 0) {
								pages.add(page);
							}
						});
						// Erase old pages, removing unverified users if they have no open
						// verification pages
						pages.forEach((page) -> {
							verificationPages.remove(page);	// remove old page from cache
							new File(page.name).delete();	// delete old page
							if (verificationPages.stream().filter(
									(p) -> p.user.equals(page.user)).count() == 0) {
								// If no verification pages are open for the user, remove the
								// user from the unverified table
								handler.updateTable(
										"DELETE FROM smalltownships.unverifiedaccounts WHERE"
										+ " username='" + page.user + "';");
							}
						});
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				try {
					TimeUnit.HOURS.sleep(1);	// Make this thread sleep for 1 hour
				} catch (InterruptedException e) {
					// Don't care if we get interrupted, just means an early check
				}
			}
		}
	}
	
	static {
		BACKGROUND_PAGECHECK_THREAD = new Thread(new PageChecker());
		BACKGROUND_PAGECHECK_THREAD.start();
	}
}
