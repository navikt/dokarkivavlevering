package no.nav.dokarkivavlevering.avlevering.domain;

import java.time.ZonedDateTime;

public class SimpleNavn extends NavnMedGyldighet {

	public SimpleNavn(String navn) {
		super(null, null, navn);
	}

	boolean isValidFor(ZonedDateTime historiskTidspunkt) {
		return true;
	}
}
