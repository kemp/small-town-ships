package me.smalltownships;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public final class Encryption {
	private Encryption() {}

	private static final Encoder base64Encode = Base64.getEncoder().withoutPadding();
	private static final Decoder base64Decode = Base64.getDecoder();

	public static final class PrimeNumberSieve {
		private final long[] sieve;
		private long maxPrime;
		private int primeCount;

		public PrimeNumberSieve(long maxPrime) {
			this.sieve = new long[(int)(1 + (maxPrime >>> 6))]; // ceil((maxPrime + 1) / 64)
			for (int i = 0; i < this.sieve.length; i++) {
				this.sieve[i] = 0xFFFFFFFFFFFFFFFFL;
			}
			// perform sieve
			this.maxPrime = 0;
			this.primeCount = 0;
			setCompositeInternal(0);
			setCompositeInternal(1);
			long max = this.sieve.length * 64L;
			for (long i = 2; this.maxPrime <= maxPrime && i > this.maxPrime;
					i = nextPrimeInternal(i)) {
				this.maxPrime = i;
				this.primeCount++;
				// in the case of i >= sqrt(max), this loop does nothing
				for (long j = i*i; j < max; j += i) {
					setCompositeInternal(j);
				}
			}
		}

		private void setCompositeInternal(long n) {
			// 1 = prime, 0 = composite
			this.sieve[(int)(n >>> 6)] &= ~(1L << (n & 0x3f));
		}

		private boolean isPrimeInternal(long n) {
			// 1 = prime, 0 = composite
			return (this.sieve[(int)(n >>> 6)] & (1L << (n & 0x3f))) != 0;
		}

		private long nextPrimeInternal(long n) {
			int i, b;
			long masked;
			i = (int)(n >>> 6);
			b = (int)((n & 0x3f) + 1);
			if (b != 64) {
				masked = this.sieve[i] & (~0L << b);
				if (masked != 0) {
					return ((long)i << 6) + Long.numberOfTrailingZeros(masked);
				}
			}
			while (++i < this.sieve.length) {
				if (this.sieve[i] != 0) {
					return ((long)i << 6) + Long.numberOfTrailingZeros(this.sieve[i]);
				}
			}
			return this.maxPrime;
		}

		public boolean isPrime(long n) {
			if (n > this.maxPrime || n < 2) {
				return false;
			} else {
				return isPrimeInternal(n);
			}
		}

		public int primesInSieve() {
			return this.primeCount;
		}

		public long maxPrimeInSieve() {
			return this.maxPrime;
		}

		public long maxNumberInSieve() {
			return this.sieve.length * 64L + 1;
		}

		public long nextPrime(long n) {
			if (n >= this.maxPrime) {
				return this.maxPrime;
			} else if (n < 2) {
				return 2;
			} else {
				return nextPrimeInternal(n);
			}
		}
	}

	private static final class PasswordHash {
		/* SHA-256 IV, pre-generated since it is identical to the FIPS IV */
		private static final int H0 = 0x6a09e667, H1 = 0xbb67ae85;
		private static final int H2 = 0x3c6ef372, H3 = 0xa54ff53a;
		private static final int H4 = 0x510e527f, H5 = 0x9b05688c;
		private static final int H6 = 0x1f83d9ab, H7 = 0x5be0cd19;

		/* SHA-256 round constants, generated at runtime since the FIPS version
		 * uses only 64 rounds, and therefore only 64 round constants */
		private static final int[] K = new int[5000];

		static {
			// use the Sieve of Eratosthenes to generate the first 5000 prime numbers
			// 5000-th prime number is 48611
			PrimeNumberSieve sieve = new PrimeNumberSieve(48611);

			// generate SHA-256 round constants as the first 32 bits of the
			// fractional part of the cube root of the first 5000 prime numbers

			// since we only need the first 32 bits of the fractional part, and
			// the integer part of the cube root of the 5000-th prime (48611) is
			// only 36 (which is 6 bits), it is safe to perform all calculations
			// using double-precision floating-point, which has 53 bits of precision
			int i;
			long j;
			for (i = 0, j = 2; i < 5000; i++, j = sieve.nextPrime(j)) {
				double cbrt = Math.cbrt(j);	// get the cube root
				double frac = cbrt - Math.floor(cbrt); // get the fractional part
				double value = Math.floor(frac * 0x1.0p32);	// extract the first 32 bits
				// convert to long first in case the 32nd bit is set, which
				// would result in a conversion to Integer.MAX_VALUE
				K[i] = (int)((long)value);
			}
		}

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
			// initialize the message schedule
			for(; i < 16; i++) {
				W[i] =	((in[off+0] & 0xff) << 24) | ((in[off+1] & 0xff) << 16) |
						((in[off+2] & 0xff) <<  8) | ((in[off+3] & 0xff) <<  0);
				off += 4;
			}

			int a = h0, b = h1, c = h2, d = h3;
			int e = h4, f = h5, g = h6, h = h7;
			int t1 = 0, t2 = 0;

			// 5000 rounds is standard for SHA256-based password hashes
			// first 16 rounds use the input bytes as the message schedule words
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
			// following rounds expand the message schedule
			for (; i < 5000; i++) {
				// generate the next value in the message schedule
				W[i & 15] += s1(W[(i-2) & 15]) + W[(i-7) & 15] + s0(W[(i-15) & 15]);
				t1 = h + W[i & 15] + K[i] + S1(e) + choice(e, f, g);
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
//		EmailVerifier.getSecureRandom().nextBytes(salt);
		new SecureRandom().nextBytes(salt);
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
