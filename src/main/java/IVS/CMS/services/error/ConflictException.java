package IVS.CMS.services.error;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}