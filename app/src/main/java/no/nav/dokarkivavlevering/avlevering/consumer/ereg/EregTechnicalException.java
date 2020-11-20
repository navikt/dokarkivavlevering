package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

public class EregTechnicalException extends RuntimeException {
	public EregTechnicalException(String message) {
		super(message);
	}

	public EregTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
