package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators;

import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class AvsluttSakerValidator {

	public static void validerAvsluttAlleSakerPaaTemaRequest(String tema, String referanse, LocalDateTime avsluttetDato, String administrativEnhet) {
		validerTema(tema);
		validerReferanse(referanse);
		validerAvsluttetDato(avsluttetDato);
		validerAdministrativEnhet(administrativEnhet);
	}

	private static void validerTema(String tema) {
		if (isBlank(tema)) {
			throw new MissingPropertiesException("tema kan ikke være null eller tom");
		}

		if (tema.length() != 3) {
			throw new MissingPropertiesException("tema må ha en lengde på 3. Mottok=" + tema);
		}
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
		if (!isBlank(administrativEnhet)) {
			if (!isNumeric(administrativEnhet)) {
				throw new MissingPropertiesException("administrativEnhet må være et heltall. Mottok=%s".formatted(administrativEnhet));
			}

			if (administrativEnhet.length() != 4) {
				throw new MissingPropertiesException("administrativEnhet må ha en lengde på 4. Mottok=%s".formatted(administrativEnhet));
			}
		}
	}
}
