package me.smalltownships;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public final class Encryption {
	
	private Encryption() {}
	
	private static final Encoder base64Encode = Base64.getEncoder().withoutPadding();
	private static final Decoder base64Decode = Base64.getDecoder();
	
	private static final class PasswordHash {
		/* SHA-256 IV */
		private static final int H0 = 0x6a09e667, H1 = 0xbb67ae85;
		private static final int H2 = 0x3c6ef372, H3 = 0xa54ff53a;
		private static final int H4 = 0x510e527f, H5 = 0x9b05688c;
		private static final int H6 = 0x1f83d9ab, H7 = 0x5be0cd19;

		/* SHA-256 round constants */
		private static final int[] K = {
				0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
				0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
				0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
				0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
				0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
				0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
				0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
				0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
				0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
				0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
				0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
				0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
				0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
				0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
				0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
				0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
		};

		private int h0 = H0, h1 = H1, h2 = H2, h3 = H3, h4 = H4, h5 = H5, h6 = H6, h7 = H7;
		private final int[] W = new int[16];
		private final byte[] buff = new byte[64];
		private int idx = 0;
		private int proc = 0;
		
		public void update(byte[] in, int off, int length) {
			if (idx != 0) {
				// fill partial block
				if (length < 64 - idx) {
					// not enough to fill partial block
					System.arraycopy(in, off, buff, idx, length);
					idx += length;
					return;
				}
				System.arraycopy(in, off, buff, idx, 64 - idx);
				off += 64 - idx;
				length -= 64 - idx;
				idx = 0;
				updateBlock(buff, 0);
				proc += 64;
			}
			// process full blocks
			while (length >= 64) {
				updateBlock(in, off);
				off += 64;
				length -= 64;
				proc += 64;
			}
			if (length != 0) {
				// save partial block
				idx = length;
				System.arraycopy(in, off, buff, 0, length);
			}
		}
		
		private void updateBlock(byte[] in, int off) {
			int i = 0;
			for(; i < 16; i++) {
				W[i] =	((in[off+0] & 0xff) << 24) | ((in[off+1] & 0xff) << 16) |
						((in[off+2] & 0xff) <<  8) | ((in[off+3] & 0xff) <<  0);
				off += 4;
			}
			
			int a = h0, b = h1, c = h2, d = h3;
			int e = h4, f = h5, g = h6, h = h7;
			int t1 = 0, t2 = 0;

			// 5000 rounds is standard for SHA256-based password hashes
			for (i = 0; i < 16; i++) {
				t1 = h + W[i] + K[i] + S1(e) + choice(e, f, g);
				t2 = S0(a) + majority(a, b, c);
				h = g;
				g = f;
				f = e;
				e = d + t1;
				d = c;
				c = b;
				b = a;
				a = t1 + t2;
			}
			for (; i < 5000; i++) {
				W[i & 15] += s1(W[(i-2) & 15]) + W[(i-7) & 15] + s0(W[(i-15) & 15]);
				t1 = h + W[i & 15] + K[i & 63] + S1(e) + choice(e, f, g);
				t2 = S0(a) + majority(a, b, c);
				h = g;
				g = f;
				f = e;
				e = d + t1;
				d = c;
				c = b;
				b = a;
				a = t1 + t2;
			}
			
			h0 += a;
			h1 += b;
			h2 += c;
			h3 += d;
			h4 += e;
			h5 += f;
			h6 += g;
			h7 += h;
		}

		public byte[] digest() {
			byte[] digest = new byte[32];
			long bits;
			int i = idx;
			
			bits = ((proc & 0xffffffffL) * 512) | (idx * 8);
			
			// apply padding
			buff[i++] = (byte)0x80;
			if (i > 56) {
				// not enough room for padding, so add another block
				for (; i < 64; i++) {
					buff[i] = 0;
				}
				updateBlock(buff, 0);
				i = 0;
			}
			// zero-pad and append length
			for (; i < 56; i++) {
				buff[i] = 0;
			}
			buff[63] = (byte)bits; bits >>>= 8;
			buff[62] = (byte)bits; bits >>>= 8;
			buff[61] = (byte)bits; bits >>>= 8;
			buff[60] = (byte)bits; bits >>>= 8;
			buff[59] = (byte)bits; bits >>>= 8;
			buff[58] = (byte)bits; bits >>>= 8;
			buff[57] = (byte)bits; bits >>>= 8;
			buff[56] = (byte)bits;
			
			updateBlock(buff, 0);
			
			// save digest, reset state, and return
			digest[31] = (byte)h7; h7 >>>= 8;
			digest[30] = (byte)h7; h7 >>>= 8;
			digest[29] = (byte)h7; h7 >>>= 8;
			digest[28] = (byte)h7; h7 = H7;
			digest[27] = (byte)h6; h6 >>>= 8;
			digest[26] = (byte)h6; h6 >>>= 8;
			digest[25] = (byte)h6; h6 >>>= 8;
			digest[24] = (byte)h6; h6 = H6;
			digest[23] = (byte)h5; h5 >>>= 8;
			digest[22] = (byte)h5; h5 >>>= 8;
			digest[21] = (byte)h5; h5 >>>= 8;
			digest[20] = (byte)h5; h5 = H5;
			digest[19] = (byte)h4; h4 >>>= 8;
			digest[18] = (byte)h4; h4 >>>= 8;
			digest[17] = (byte)h4; h4 >>>= 8;
			digest[16] = (byte)h4; h4 = H4;
			digest[15] = (byte)h3; h3 >>>= 8;
			digest[14] = (byte)h3; h3 >>>= 8;
			digest[13] = (byte)h3; h3 >>>= 8;
			digest[12] = (byte)h3; h3 = H3;
			digest[11] = (byte)h2; h2 >>>= 8;
			digest[10] = (byte)h2; h2 >>>= 8;
			digest[ 9] = (byte)h2; h2 >>>= 8;
			digest[ 8] = (byte)h2; h2 = H2;
			digest[ 7] = (byte)h1; h1 >>>= 8;
			digest[ 6] = (byte)h1; h1 >>>= 8;
			digest[ 5] = (byte)h1; h1 >>>= 8;
			digest[ 4] = (byte)h1; h1 = H1;
			digest[ 3] = (byte)h0; h0 >>>= 8;
			digest[ 2] = (byte)h0; h0 >>>= 8;
			digest[ 1] = (byte)h0; h0 >>>= 8;
			digest[ 0] = (byte)h0; h0 = H0;
			
			return digest;
		}
		
		
		private static int rotl(int x, int s) {
			return (x << s) | (x >>> -s);
		}
		
		private static int choice(int x, int y, int z) {
			// equivalent to (x & y) | (~x & z)
			return z ^ (x & (y ^ z));
		}
		
		private static int majority(int x, int y, int z) {
			// equivalent to (x & y) ^ (x & z) ^ (y & z)
			return (x & y) ^ (z & (x ^ y));
		}
		
		private static int s0(int x) {
			return rotl(x, 25) ^ rotl(x, 14) ^ (x >>>  3);
		}
		
		private static int s1(int x) {
			return rotl(x, 15) ^ rotl(x, 13) ^ (x >>> 10);
		}
		
		private static int S0(int x) {
			return rotl(x, 30) ^ rotl(x, 19) ^ rotl(x, 10);
		}
		
		private static int S1(int x) {
			return rotl(x, 26) ^ rotl(x, 21) ^ rotl(x, 7);
		}
	}
	
	private static byte[] hashPassword(String password, byte[] salt) {
		// get the password's bytes (in UTF-8)
		byte[] pass;
		byte[] passBytes = password.getBytes(StandardCharsets.UTF_8);
		// ensure we have exactly 32 bytes
		if (passBytes.length == 32) {
			pass = passBytes;
		} else {
			// if we have less than 32 bytes, zero-pad them
			pass = Arrays.copyOf(passBytes, 32);
			if (passBytes.length > 32) {
				// if we have more than 32 bytes, XOR each successive block of
				// 32 bytes, zero-padding the last block
				for (int i = 32; i < passBytes.length; i++) {
					pass[i&31] ^= passBytes[i];
				}
			}
		}
		// compute the (simple) salted hash
		PasswordHash hash = new PasswordHash();
		// compute the digest as SHA256(salt-password-salt)
		hash.update(salt, 0, 16);
		hash.update(pass, 0, 32);
		hash.update(salt, 0, 16);
		return hash.digest();
	}
	
	/**
	 * Compute a hash of the input password using a randomly-generated 128-bit salt.
	 * 
	 * @param password The password to hash
	 * @return The hash value, 66 characters long, in the format of <i>salt</i>$<i>hash</i>
	 */
	public static String hashPassword(String password) {
		// get the random salt
		byte[] salt = new byte[16];
		EmailVerifier.getSecureRandom().nextBytes(salt);
		// compute the password's
		byte[] hash = hashPassword(password, salt);
		// Base64-encode the salt and hash
		String saltStr = new String(base64Encode.encode(salt), StandardCharsets.UTF_8);
		String hashStr = new String(base64Encode.encode(hash), StandardCharsets.UTF_8);
		// output the hash string
		return saltStr + "$" + hashStr;
	}
	
	/**
	 * Computes a hash of the input password using the salt 
	 * 
	 * @param password
	 * @param expect
	 * @return
	 */
	public static boolean verifyPassword(String password, String expect) {
		if (expect.length() != 66) {
			throw new IllegalArgumentException("Invalid password salt-hash string: " + expect);
		}
		int split = expect.indexOf('$');
		if (split < 0) {
			throw new IllegalArgumentException("Invalid password salt-hash string: " + expect);
		}
		// split the salt and hash
		String saltStr = expect.substring(0, split);
		String hashStr = expect.substring(split+1);
		// decode the salt and hash
		byte[] salt = base64Decode.decode(saltStr.getBytes(StandardCharsets.UTF_8));
		byte[] hash = base64Decode.decode(hashStr.getBytes(StandardCharsets.UTF_8));
		// ensure the salt and hash are the correct length
		if (salt.length != 16) {
			throw new IllegalArgumentException("Invalid salt string: " + saltStr);
		} else if (hash.length != 32) {
			throw new IllegalArgumentException("Invalid hash string: " + hashStr);
		}
		// compute the input password's hash
		byte[] newHash = hashPassword(password, salt);
		boolean matches = true;
		// timing-resistant (probably unnecessary) comparison of the hashes
		for (int i = 0; i < 32; i++) {
			matches &= (hash[i] == newHash[i]);
		}
		return matches;
	}
}
