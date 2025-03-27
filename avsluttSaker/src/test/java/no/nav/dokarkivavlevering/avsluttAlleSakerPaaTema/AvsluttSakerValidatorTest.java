package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;


import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AvsluttSakerValidatorTest {

	private static final String TEMA = "FAR";
	private static final String REFERANSE = "MMA-7899";
	private static final LocalDateTime AVSLUTTET_DATO = LocalDateTime.now();
	private static final String ADMINISTRATIV_ENHET = "1294";

	@ParameterizedTest
	@MethodSource
	void skalValidere(String tema, String referanse, LocalDateTime avsluttetDato, String administrativEnhet) {
		assertThatNoException().isThrownBy(() ->
				validerAvsluttAlleSakerPaaTemaRequest(tema, referanse, avsluttetDato, administrativEnhet));
	}

	public static Stream<Arguments> skalValidere() {
		return Stream.of(
				Arguments.of(TEMA, REFERANSE, AVSLUTTET_DATO, ADMINISTRATIV_ENHET),
				Arguments.of(TEMA, REFERANSE, null, ADMINISTRATIV_ENHET),
				Arguments.of(TEMA, REFERANSE, AVSLUTTET_DATO, null),
				Arguments.of(TEMA, REFERANSE, null, null)
		);
	}

	@ParameterizedTest
	@MethodSource
	void skalKasteExceptionDersomTemaErUgyldig(String tema, String feilmelding) {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(tema, REFERANSE, AVSLUTTET_DATO, ADMINISTRATIV_ENHET))
				.withMessage(feilmelding);
	}

	public static Stream<Arguments> skalKasteExceptionDersomTemaErUgyldig() {
		return Stream.of(
				Arguments.of("", "tema har ikke lengde 3. Mottok tema="),
				Arguments.of(" ", "tema har ikke lengde 3. Mottok tema= "),
				Arguments.of(null, "tema har ikke lengde 3. Mottok tema=null"),
				Arguments.of("FA", "tema har ikke lengde 3. Mottok tema=FA"),
				Arguments.of("FARR", "tema har ikke lengde 3. Mottok tema=FARR")
		);
	}

	@ParameterizedTest
	@MethodSource
	void skalKasteExceptionDersomReferanseErForLang(String referanse, String feilmelding) {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(TEMA, referanse, AVSLUTTET_DATO, ADMINISTRATIV_ENHET))
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
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, ugyldigAvsluttetDato, ADMINISTRATIV_ENHET))
				.withMessageContaining("avsluttetDato kan ikke være i fremtiden.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"abcd", "123a"})
	void skalKasteExceptionDersomAdministrativEnhetIkkeErEtHeltall(String administrativEnhet) {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, AVSLUTTET_DATO, administrativEnhet))
				.withMessage("administrativEnhet må være et heltall. Mottok=%s".formatted(administrativEnhet));
	}

	@ParameterizedTest
	@ValueSource(strings = {"123", "12345"})
	void skalKasteExceptionDersomAdministrativEnhetErFeilLengde(String administrativEnhet) {
		assertThatExceptionOfType(MissingPropertiesException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, AVSLUTTET_DATO, administrativEnhet))
				.withMessage("administrativEnhet må ha en lengde på 4. Mottok=%s".formatted(administrativEnhet));
	}

}