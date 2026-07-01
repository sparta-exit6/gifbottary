package com.example.gifbottary.common.util;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 기프티콘 핀번호를 안전하게 저장하기 위한 암호화 유틸입니다.
 * 실제 복호화가 필요한 값은 암호문으로 보관하고,
 * 중복 체크는 별도의 결정적 해시값으로 처리합니다.
 */
@Component
public class PinEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;

    private final byte[] secretKey;

    public PinEncryptor(@Value("${pin.encryption.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new ServiceException(ErrorCode.INVALID_PIN_ENCRYPTION_KEY);
        }
        this.secretKey = Arrays.copyOf(keyBytes, keyBytes.length);
    }

    public String encrypt(String rawPin) {
        if (rawPin == null || rawPin.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_INPUT);
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(secretKey, ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );

            byte[] encrypted = cipher.doFinal(rawPin.trim().getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCode.PIN_ENCRYPTION_FAILED);
        }
    }

    public String decrypt(String encryptedPin) {
        if (encryptedPin == null || encryptedPin.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_INPUT);
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPin);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(secretKey, ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCode.PIN_DECRYPTION_FAILED);
        }
    }

    /**
     * 중복 핀번호 검사를 위해 항상 같은 결과를 반환하는 해시값을 생성합니다.
     * 이 값은 복호화 용도가 아니라 중복 체크 전용입니다.
     */
    public String hash(String rawPin) {
        if (rawPin == null || rawPin.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_INPUT);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawPin.trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCode.PIN_ENCRYPTION_FAILED);
        }
    }
}
