package no.nav.dokarkivavlevering.core.exception;

public class DokarkivavleveringFunctionalException extends RuntimeException {

	public DokarkivavleveringFunctionalException(String message) {
		super(message);
	}

	public DokarkivavleveringFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
