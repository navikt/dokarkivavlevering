package no.nav.dokarkivavlevering.avlevering.common;

import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class JournaldatoMapperTest {
	private static final String DATO_JOURNAL = "2020-12-01";
	private static final String DATO_DOKUMENT = "2020-12-02";
	private static final String DATO_DOKUMENT_FERDIG = "2020-12-03";
	private static final String DATO_ENDRET = "2020-12-04";

	private final JournaldatoMapper mapper = new JournaldatoMapper();

	@Test
	void shouldMapJournaldatoWhenJournalDatoNotNull() {
		Date journalDato = mapper.mapJournaldato(baseJournalpostBuilder().build());
		assertThat(journalDato).isEqualToIgnoringHours(DATO_JOURNAL);
	}

	@Test
	void shouldMapDokumentDatoWhenJournalDatoNull() {
		Date journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.build());
		assertThat(journalDato).isEqualToIgnoringHours(DATO_DOKUMENT);
	}

	@Test
	void shouldMapDokumentFerdigDatoWhenDokumentDatoNull() {
		Date journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.datoDokument(null)
				.build());
		assertThat(journalDato).isEqualToIgnoringHours(DATO_DOKUMENT_FERDIG);
	}

	@Test
	void shouldMapEndretDatoWhenDokumentFerdigDatoNull() {
		Date journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.datoDokument(null)
				.dok(Collections.singletonList(DokumentInfo.builder()
						.relTilknyttetSom("HOVEDDOKUMENT")
						.datoFerdig(null)
						.build()))
				.build());
		assertThat(journalDato).isEqualToIgnoringHours(DATO_ENDRET);
	}

	private Journalpost.JournalpostBuilder baseJournalpostBuilder() {
		return Journalpost.builder()
				.datoJournal(mapToDate(DATO_JOURNAL))
				.datoDokument(mapToDate(DATO_DOKUMENT))
				.datoEndret(mapToDate(DATO_ENDRET))
				.dok(Collections.singletonList(DokumentInfo.builder()
						.relTilknyttetSom("HOVEDDOKUMENT")
						.datoFerdig(mapToDate(DATO_DOKUMENT_FERDIG))
						.build()));
	}

	private Date mapToDate(final String date) {
		return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC));
	}
}