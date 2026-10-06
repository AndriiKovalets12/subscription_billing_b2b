package dev.security.refresh_tokens;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String token, String mes) {
        super(mes + ":" + token);
    }
}
