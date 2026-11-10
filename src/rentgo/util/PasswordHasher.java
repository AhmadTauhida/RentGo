package rentgo.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hashes passwords with PBKDF2 supplied by the JDK. */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private PasswordHasher() { }
    public static String hash(char[] password) { try { byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt); byte[] digest = derive(password, salt, ITERATIONS); return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest); } catch (GeneralSecurityException e) { throw new IllegalStateException("Password tidak dapat diproses", e); } }
    public static boolean verify(char[] password, String stored) { try { String[] parts = stored.split(":"); if (parts.length != 3) return false; int iterations = Integer.parseInt(parts[0]); byte[] salt = Base64.getDecoder().decode(parts[1]); byte[] expected = Base64.getDecoder().decode(parts[2]); return MessageDigest.isEqual(expected, derive(password, salt, iterations)); } catch (RuntimeException | GeneralSecurityException e) { return false; } }
    private static byte[] derive(char[] password, byte[] salt, int iterations) throws GeneralSecurityException { return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(new PBEKeySpec(password, salt, iterations, 256)).getEncoded(); }
}