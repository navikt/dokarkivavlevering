package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.dateToXMLGregorianCalendar;

class EndringsloggMapperTest {

	private final EndringsloggMapper mapper = new EndringsloggMapper();

	@Test
	void shouldMap() {
		Date date = Date.from(Instant.now());
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

		final Endring endring = mapper.map(arkivendring, uuid);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getReferanseArkivenhet()).isEqualTo(uuid.toString());
		softly.assertThat(endring.getReferanseMetadata()).isEqualTo("Saksrelasjon.sakId");
		softly.assertThat(endring.getEndretDato()).isEqualTo(dateToXMLGregorianCalendar(arkivendring.getTidspunkt()));
		softly.assertThat(endring.getEndretAv()).isEqualTo("JonBlund");
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo("123");
		softly.assertThat(endring.getNyVerdi()).isEqualTo("1234");
		softly.assertAll();
	}

	@Test
	void shouldMapJournalpostStatus() {
		Date date = Date.from(Instant.now());
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

		final Endring endring = mapper.map(arkivendring, uuid);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getReferanseArkivenhet()).isEqualTo(uuid.toString());
		softly.assertThat(endring.getReferanseMetadata()).isEqualTo("Journalpost.journalpostStatus");
		softly.assertThat(endring.getEndretDato()).isEqualTo(dateToXMLGregorianCalendar(arkivendring.getTidspunkt()));
		softly.assertThat(endring.getEndretAv()).isEqualTo("JonBlund");
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo("JOURNALFØRT");
		softly.assertThat(endring.getNyVerdi()).isEqualTo("FERDIGSTILT");
		softly.assertAll();
	}

	@Test
	void shouldMapIngenVerdiWhenFraVerdiIsNull() {
		Date date = Date.from(Instant.now());
		UUID uuid = UUID.randomUUID();

		Arkivendring arkivendring = Arkivendring.builder()
				.id(1L)
				.element("Saksrelasjon.sakId")
				.tidspunkt(date)
				.utfoertAv("B000000")
				.utfoertAvBeriketNavn("JonBlund")
				.fraVerdi(null)
				.tilVerdi("1234")
				.build();

		final Endring endring = mapper.map(arkivendring, uuid);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endring.getTidligereVerdi()).isEqualTo(Arkivendring.INGEN_VERDI);
		softly.assertAll();
	}
}