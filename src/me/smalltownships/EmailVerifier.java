package me.smalltownships;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Generate a web page to verify an email address
 * 
 * @author Joshua Fehrenbach
 */
public class EmailVerifier {

	private static final SecureRandom rand;
	private static final String secureRandAlg;
	private static final AtomicLong uniqueSeed;

	private static final Encoder base64Encode;
	private static final TreeMap<String,VerificationCode> verificationCodes;
	private static final Queue<Message> emailQueue;

	private static final String serverEmailAccount;
	private static final String serverEmailPassword;
	
	private static final String WEB_ADDRESS;
	
	private static final Session EMAIL_SESSION;
	private static final Thread BACKGROUND_PAGECHECK_THREAD;
	private static final Thread BACKGROUND_EMAIL_THREAD;
	private static final Address FROM_ADDRESS;
	
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
	public static boolean verifyAccount(String userCode) {
		String user;
		ResultSet rs;
		System.out.println(userCode);
		if (userCode == null || userCode.isEmpty()) {
			return false;
		}
		synchronized (verificationCodes) {
			if (!verificationCodes.containsKey(userCode)) {
				return false;
			}
			user = verificationCodes.get(userCode).username;
		}
		System.out.println(user);
		try (MySQLHandler sqlHandler = new MySQLHandler()) {
			rs = sqlHandler.callProcedure("Search_Unverified_User(?)", 1, new String[] {user});
			if (!rs.next()) {		// No entry found
				rs.close();
				return false;
			}
			sqlHandler.callProcedure("Verify_User(?)", 1, new String[] {user});
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
	public static void createVerificationPage(String email, String user, String date)
			throws IOException {
		VerificationCode page;
		// Ensure that the address created is unique
		synchronized (verificationCodes) {
			String code;
			byte[] randBytes = new byte[16];	// 128-bit random integer
			do {
				rand.nextBytes(randBytes);
				// Ensure the encoded string is in UTF-8 format
				code = new String(base64Encode.encode(randBytes), StandardCharsets.UTF_8);
				page = new VerificationCode(code, user, date);
			} while (verificationCodes.containsKey(code));
			// Now have a unique page
			verificationCodes.put(code, page);
		}
		try {
			// Create the email
			Message msg = new MimeMessage(EMAIL_SESSION);
			// Set the sender
			msg.setFrom(FROM_ADDRESS);
			// Set the recipient
			msg.setRecipient(Message.RecipientType.TO,
					new InternetAddress(email));
			// Set the subject
			msg.setSubject("Small Town Ships Account Verification");
			// Set the contents of the email (in HTML format)
			msg.setDataHandler(new DataHandler(new HTMLDataSource(
				"<h1>Thank you for registering with " +
				"Small Town Ships!</h1><br><h3>Please <a href=\"" +
				WEB_ADDRESS + "?id=" + page.code + "\">" +
				"click here</a> to verify your account.</h3>")));
			// Save the message
			msg.saveChanges();
			// Add the message to the queue
			synchronized (emailQueue) {
				emailQueue.add(msg);
				emailQueue.notifyAll();
			}
		} catch (MessagingException e) {
			synchronized (verificationCodes) {
				verificationCodes.remove(page.code);
			}
			throw new RuntimeException(e);
		}
	}
	
	static {
		/***** SecureRandom Setup *****/
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
				// No preferred SecureRandom instance found, so use default
				tmp = new SecureRandom();
			}
		}
		rand = tmp;
		secureRandAlg = rand.getAlgorithm();
		uniqueSeed = new AtomicLong(System.nanoTime()*494852194791354853L + Runtime.getRuntime().freeMemory());
		rand.setSeed(rand.generateSeed(16));
		rand.setSeed(rand.nextLong() ^ getUniqueSeed() ^ rand.nextLong());
		
		/***** Verification Setup *****/
		verificationCodes = new TreeMap<>();
		base64Encode = Base64.getUrlEncoder().withoutPadding();
		emailQueue = new LinkedList<>();
		
		/***** Email Setup *****/
		// Retrieve the email account's username, password, and SMTP properties from the
		// config file
		String username, password;
		String xmlPath = System.getProperty("catalina.home") +
				"\\webapps\\SmallTownShipsConfig.xml";
		try {
			// Open the config file
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(xmlPath));
			Element root = doc.getDocumentElement();
			root.normalize();
			// Extract email config
			Element node = (Element) root.getElementsByTagName("email").item(0);
			// Get account name
			username = node.getElementsByTagName("username").item(0).getTextContent();
			// Get account password
			password = node.getElementsByTagName("password").item(0).getTextContent();
			// Get SMTP settings
			NodeList props = node.getElementsByTagName("property");
			// Parse SMTP settings
			for (int i = 0; i < props.getLength(); i++) {
				Element e = (Element) props.item(i);
				String key = e.getElementsByTagName("key").item(0).getTextContent();
				String value = e.getElementsByTagName("value").item(0).getTextContent();
				System.setProperty(key, value);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
		serverEmailAccount = username;
		serverEmailPassword = password;
		WEB_ADDRESS = "http://localhost:8080/small-town-ships/verify.jsp";

		/***** Background Process Setup *****/
		Properties props = System.getProperties();
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(serverEmailAccount, serverEmailPassword);
			}
		};
		Address from;
		try {
			from = new InternetAddress(username);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
		EMAIL_SESSION = Session.getInstance(props, auth);
		FROM_ADDRESS = from;
		BACKGROUND_PAGECHECK_THREAD = new PageChecker();
		BACKGROUND_EMAIL_THREAD = new Emailer();
		BACKGROUND_PAGECHECK_THREAD.start();
		BACKGROUND_EMAIL_THREAD.start();
	}

	private static class VerificationCode implements Comparable<VerificationCode> {
		
		final String code;
		final String username;
		final Date date;
		
		VerificationCode(String code, String username, Date date) {
			this.code = code;
			this.username = username;
			this.date = date;
		}
		
		VerificationCode(String code, String username, String date) {
			this(code, username, Date.valueOf(date));
		}
		
		@Override
		public int compareTo(VerificationCode o) {
			return code.compareTo(o.code);
		}

		@Override
		public int hashCode() {
			return code.hashCode() ^ username.hashCode() ^ date.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof VerificationCode)) {
				return false;
			} else {
				return ((VerificationCode)obj).code.equals(this.code);
			}
		}

		@Override
		public String toString() {
			return "[" + code + "," + username + "," + date.toString() + "]";
		}
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
	
	private static class PageChecker extends Thread {
		
		private void removeOldPages() {
			// Block creating new pages while removing the old pages
			synchronized (verificationCodes) {
				Date date = Date.valueOf(LocalDate.now().minusDays(2));
				ArrayList<VerificationCode> oldCodes = new ArrayList<>();
				// Get all of the old codes
				verificationCodes.forEach((code, verificationCode) -> {
					if (date.compareTo(verificationCode.date) > 0) {
						oldCodes.add(verificationCode);
					}
				});
				if (!oldCodes.isEmpty()) {

					MySQLHandler sqlHandler = new MySQLHandler();
						// Remove the old codes from the cache
						oldCodes.forEach((code) -> {
							verificationCodes.remove(code.code);
							sqlHandler.callProcedure("Delete_User(?)", 1, new String[] {code.username});
						});
				}
			}
		}
		
		public void run() {
			// endless loop, because this thread will always be running
			while (true) {
				// Remove all old pages and users that took too long to verify
				removeOldPages();
				try {
					TimeUnit.HOURS.sleep(1);	// Make this thread sleep for 1 hour
				} catch (InterruptedException e) {
					// If we get interrupted, exit the endless loop
					break;
				}
			}
		}
	}
	
	private static class Emailer extends Thread {

		@Override
		public void run() {
			LinkedList<Message> messages = new LinkedList<>();
			// endless loop, because this thread will always be running
			while (true) {
				// Pull all pending messages from the queue
				synchronized (emailQueue) {
					while (!emailQueue.isEmpty()) {
						messages.add(emailQueue.poll());
					}
					// Check whether anything was read
					if (messages.isEmpty()) {
						try {
							// Make this thread wait for at least 10 seconds
							emailQueue.wait(10000);	// 10000ms = 10s
						} catch (InterruptedException e) { }
						continue;
					}
				}
				// Send the messages
				try {
					// Connect to SMTP server
					Transport t = EMAIL_SESSION.getTransport();
					t.connect(serverEmailAccount, serverEmailPassword);
					do {
						// Send individual messages
						Message msg = messages.poll();
						t.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
					} while (!messages.isEmpty());
					// Close SMTP server connection
					t.close();
				} catch (MessagingException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
