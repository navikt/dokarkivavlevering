package no.nav.dokarkivavlevering.avlevering.exception;

/**
 * Base funksjonell feil
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AvleveringFunctionalException extends RuntimeException {

	public AvleveringFunctionalException(String message) {
		super(message);
	}

	public AvleveringFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
