package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EndringsloggMapperTest {

	private final EndringsloggMapper mapper = new EndringsloggMapper();

	@Test
	void shouldMap() {
		LocalDateTime date = LocalDateTime.now();
		UUID uuid = UUID.randomUUID();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element("Saksrelasjon.sakId")
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi("123")
				.tilVerdi("1234")
				.build();

		Optional<Endring> endringOptional = mapper.map(arkivendring, uuid);
		assertThat(endringOptional).isNotEmpty();
		Endring endring = endringOptional.get();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getReferanseArkivenhet()).isEqualTo(uuid.toString());
		softly.assertThat(endring.getReferanseMetadata()).isEqualTo("M003");
		softly.assertThat(endring.getEndretDato()).isEqualTo(arkivendring.getTidspunkt());
		softly.assertThat(endring.getEndretAv()).isEqualTo("JonBlund");
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo("123");
		softly.assertThat(endring.getNyVerdi()).isEqualTo("1234");
		softly.assertAll();
	}

	@Test
	void shouldMapJournalpostStatus() {
		LocalDateTime date = LocalDateTime.now();
		UUID uuid = UUID.randomUUID();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element("Journalpost.journalpostStatus")
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi("J")
				.tilVerdi("FS")
				.build();

		Optional<Endring> endringOptional = mapper.map(arkivendring, uuid);
		assertThat(endringOptional).isNotEmpty();
		Endring endring = endringOptional.get();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getReferanseArkivenhet()).isEqualTo(uuid.toString());
		softly.assertThat(endring.getReferanseMetadata()).isEqualTo("M053");
		softly.assertThat(endring.getEndretDato()).isEqualTo(arkivendring.getTidspunkt());
		softly.assertThat(endring.getEndretAv()).isEqualTo("JonBlund");
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo("JOURNALFØRT");
		softly.assertThat(endring.getNyVerdi()).isEqualTo("FERDIGSTILT");
		softly.assertAll();
	}

	static Stream<Arguments> shouldMapIngenVerdiWhenFraVerdiIs() {
		return Stream.of(
				Arguments.arguments((String) null),
				Arguments.arguments("null"),
				Arguments.arguments("  "),
				Arguments.arguments(" NULL "),
				Arguments.arguments("")
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapIngenVerdiWhenFraVerdiIs(String fraVerdi) {
		LocalDateTime date = LocalDateTime.now();
		UUID uuid = UUID.randomUUID();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element("Saksrelasjon.sakId")
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi(fraVerdi)
				.tilVerdi("1234")
				.build();

		Optional<Endring> endringOptional = mapper.map(arkivendring, uuid);
		assertThat(endringOptional).isNotEmpty();
		Endring endring = endringOptional.get();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo(Arkivendring.INGEN_VERDI);
		softly.assertAll();
	}

	@Test
	void shouldMapEmptyOptionalWhenToAndFromAreBothEmpty() {
		LocalDateTime date = LocalDateTime.now();
		UUID uuid = UUID.randomUUID();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element("Saksrelasjon.sakId")
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi(null)
				.tilVerdi(null)
				.build();

		Optional<Endring> endringOptional = mapper.map(arkivendring, uuid);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endringOptional).isEmpty();
		softly.assertAll();
	}

	@Test
	void shouldMapEmptyAndLoggWarningWhenReferanseArkivenhetAndReferanseMetadataErNull() {
		LocalDateTime date = LocalDateTime.now();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element(null)
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi("J")
				.tilVerdi("FS")
				.build();

		Optional<Endring> endringOptional = mapper.map(arkivendring, null);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endringOptional).isEmpty();
		softly.assertAll();
	}
}