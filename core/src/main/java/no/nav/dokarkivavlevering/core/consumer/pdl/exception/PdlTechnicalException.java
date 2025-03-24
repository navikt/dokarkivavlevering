package no.nav.dokarkivavlevering.core.consumer.pdl.exception;

import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringTechnicalException;

public class PdlTechnicalException extends DokarkivavleveringTechnicalException {
    public PdlTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
