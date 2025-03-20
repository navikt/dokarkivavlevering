package no.nav.dokarkivavlevering.core.consumer.pdl.exception;

import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringFunctionalException;

public class PdlFunctionalException extends DokarkivavleveringFunctionalException {
    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
