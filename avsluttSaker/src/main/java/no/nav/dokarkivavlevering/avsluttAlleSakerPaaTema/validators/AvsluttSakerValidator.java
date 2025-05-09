package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators;

import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

public class AvsluttSakerValidator {

	public static void validerAvsluttAlleSakerPaaTemaRequest(String referanse, LocalDateTime avsluttetDato, String administrativEnhet) {
		validerReferanse(referanse);
		validerAvsluttetDato(avsluttetDato);
		validerAdministrativEnhet(administrativEnhet);
	}

	private static void validerReferanse(String referanse) {
		if (isBlank(referanse)) {
			throw new MissingPropertiesException("referanse kan ikke være null eller tom");
		}

		if (referanse.length() > 40) {
			throw new MissingPropertiesException("referanse kan ikke være lengre enn 40 tegn. Mottok=%s".formatted(referanse));
		}
	}

	private static void validerAvsluttetDato(LocalDateTime avsluttetDato) {
		if (avsluttetDato != null) {
			LocalDateTime naatid = LocalDateTime.now().plusSeconds(3);

			if (avsluttetDato.isAfter(naatid)) {
				throw new MissingPropertiesException("avsluttetDato kan ikke være i fremtiden. Nåtid er=%s og mottok=%s".formatted(naatid, avsluttetDato));
			}
		}
	}

	private static void validerAdministrativEnhet(String administrativEnhet) {
		if (!isEmpty(administrativEnhet) && isBlank(administrativEnhet)) {
			throw new MissingPropertiesException("administrativEnhet kan ikke være tom. Mottok=%s".formatted(administrativEnhet));
		}
		if (!isBlank(administrativEnhet)) {
			if (administrativEnhet.length() > 40) {
				throw new MissingPropertiesException("administrativEnhet kan ikke være lengre enn 40 tegn. Mottok=%s".formatted(administrativEnhet));
			}
		}
	}
}
