package no.nav.dokarkivavlevering.core.consumer.pdl.exception;

import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringFunctionalException;

public class PersonIkkeFunnetException extends DokarkivavleveringFunctionalException {
    public PersonIkkeFunnetException(String message) {
        super(message);
    }

}
