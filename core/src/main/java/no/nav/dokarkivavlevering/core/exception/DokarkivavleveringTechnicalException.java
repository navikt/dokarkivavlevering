package no.nav.dokarkivavlevering.core.exception;

public class DokarkivavleveringTechnicalException extends RuntimeException {

	public DokarkivavleveringTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
	public DokarkivavleveringTechnicalException(String message) {
		super(message);
	}
}
