package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

public class EregFunctionalException extends RuntimeException{

	public EregFunctionalException(String message) {
		super(message);
	}

	public EregFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
