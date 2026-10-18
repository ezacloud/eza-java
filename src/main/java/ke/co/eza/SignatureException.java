package ke.co.eza;

public class SignatureException extends RuntimeException {
    public SignatureException(String message) {
        super("eza webhook signature: " + message);
    }
}
