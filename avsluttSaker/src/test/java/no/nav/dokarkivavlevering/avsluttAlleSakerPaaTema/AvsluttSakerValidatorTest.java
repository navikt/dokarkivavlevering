package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;


import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AvsluttSakerValidatorTest {

	private static final String REFERANSE = "MMA-7899";
	private static final LocalDateTime AVSLUTTET_DATO = LocalDateTime.now();
	private static final String ADMINISTRATIV_ENHET = "ENHET ENHETSEN";

	@ParameterizedTest
	@MethodSource
	void skalValidere(String referanse, LocalDateTime avsluttetDato, String administrativEnhet) {
		assertThatNoException().isThrownBy(() ->
				validerAvsluttAlleSakerPaaTemaRequest(referanse, avsluttetDato, administrativEnhet));
	}

	public static Stream<Arguments> skalValidere() {
		return Stream.of(
				Arguments.of(REFERANSE, AVSLUTTET_DATO, ADMINISTRATIV_ENHET),
				Arguments.of(REFERANSE, null, ADMINISTRATIV_ENHET),
				Arguments.of(REFERANSE, AVSLUTTET_DATO, ""),
				Arguments.of(REFERANSE, null, null)
		);
	}

	@ParameterizedTest
	@MethodSource
	void skalKasteExceptionDersomReferanseErForLang(String referanse, String feilmelding) {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(referanse, AVSLUTTET_DATO, ADMINISTRATIV_ENHET))
				.withMessage(feilmelding);
	}

	public static Stream<Arguments> skalKasteExceptionDersomReferanseErForLang() {
		return Stream.of(
				Arguments.of("", "referanse kan ikke være null eller tom"),
				Arguments.of(" ", "referanse kan ikke være null eller tom"),
				Arguments.of(null, "referanse kan ikke være null eller tom"),
				Arguments.of("REFERANSE_I_JIRA_OVER_MAKSGRENSEN_40_TEGN", "referanse kan ikke være lengre enn 40 tegn. Mottok=REFERANSE_I_JIRA_OVER_MAKSGRENSEN_40_TEGN")
		);
	}

	@Test
	void skalKasteExceptionDersomAvsluttetDatoErIFremtiden() {
		LocalDateTime ugyldigAvsluttetDato = LocalDateTime.now().plusMinutes(1);

		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(REFERANSE, ugyldigAvsluttetDato, ADMINISTRATIV_ENHET))
				.withMessageContaining("avsluttetDato kan ikke være i fremtiden.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void skalGodtaAtAdministrativEnhetIkkeErSatt(String administrativEnhet) {
		assertThatNoException()
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(REFERANSE, AVSLUTTET_DATO, administrativEnhet));
	}

	@Test
	void skalKasteExceptionDersomAdministrativEnhetErOver40Tegn() {
		String administrativEnhet = "detteErEnAltForLangStrengPåOver40TegnKanskje";
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(REFERANSE, AVSLUTTET_DATO, administrativEnhet))
				.withMessage("administrativEnhet kan ikke være lengre enn 40 tegn. Mottok=" + administrativEnhet);
	}

	@Test
	void skalKasteExceptionDersomAdministrativEnhetErTom() {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(REFERANSE, AVSLUTTET_DATO, " "))
				.withMessage("administrativEnhet kan ikke være tom. Mottok=%s".formatted(" "));
	}
}