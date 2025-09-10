package com.jFastApi.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class DefaultPasswordEncoder implements PasswordEncoder {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 512; // bits
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        try {
            // Generate a random 16-byte salt
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);

            // Hash the password
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            // Encode salt + hash as Base64 for storage: salt:hash
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] computedHash = skf.generateSecret(spec).getEncoded();

            // Constant-time comparison to prevent timing attacks
            if (computedHash.length != storedHash.length) return false;
            int result = 0;
            for (int i = 0; i < computedHash.length; i++) {
                result |= computedHash[i] ^ storedHash[i];
            }
            return result == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
