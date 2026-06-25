package com.example.gifbottary.common.util;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 핀 번호를 DB에 평문으로 저장하지 않기 위한 AES 유틸입니다.
 */
@Component
public class PinEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final byte[] SECRET_KEY = "GifBottaryPinKey".getBytes(StandardCharsets.UTF_8);

    public String encrypt(String rawPin) {
        if (rawPin == null || rawPin.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_INPUT);
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY, ALGORITHM));
            byte[] encrypted = cipher.doFinal(rawPin.trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCode.PIN_ENCRYPTION_FAILED);
        }
    }

    public String decrypt(String encryptedPin) {
        if (encryptedPin == null || encryptedPin.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_INPUT);
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY, ALGORITHM));
            byte[] decoded = Base64.getDecoder().decode(encryptedPin);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCode.PIN_DECRYPTION_FAILED);
        }
    }
}
