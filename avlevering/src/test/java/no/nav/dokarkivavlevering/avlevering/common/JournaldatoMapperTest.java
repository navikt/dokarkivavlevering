package no.nav.dokarkivavlevering.avlevering.common;

import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JournaldatoMapperTest {
	private static final LocalDate DATO_JOURNAL = LocalDate.parse("2020-12-01");
	private static final LocalDate DATO_DOKUMENT = LocalDate.parse("2020-12-02");
	private static final LocalDate DATO_DOKUMENT_FERDIG = LocalDate.parse("2020-12-03");
	private static final LocalDate DATO_ENDRET = LocalDate.parse("2020-12-04");

	private final JournaldatoMapper mapper = new JournaldatoMapper();

	@Test
	void shouldMapJournaldatoWhenJournalDatoNotNull() {
		LocalDateTime journalDato = mapper.mapJournaldato(baseJournalpostBuilder().build());
		assertThat(journalDato.toLocalDate()).isEqualTo(DATO_JOURNAL);
	}

	@Test
	void shouldMapDokumentDatoWhenJournalDatoNull() {
		LocalDateTime journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.build());
		assertThat(journalDato.toLocalDate()).isEqualTo(DATO_DOKUMENT);
	}

	@Test
	void shouldMapDokumentFerdigDatoWhenDokumentDatoNull() {
		LocalDateTime journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.datoDokument(null)
				.build());
		assertThat(journalDato.toLocalDate()).isEqualTo(DATO_DOKUMENT_FERDIG);
	}

	@Test
	void shouldMapEndretDatoWhenDokumentFerdigDatoNull() {
		LocalDateTime journalDato = mapper.mapJournaldato(baseJournalpostBuilder()
				.datoJournal(null)
				.datoDokument(null)
				.dok(Collections.singletonList(DokumentInfo.builder()
						.relTilknyttetSom("HOVEDDOKUMENT")
						.datoFerdig(null)
						.build()))
				.build());
		assertThat(journalDato.toLocalDate()).isEqualTo(DATO_ENDRET);
	}

	private Journalpost.JournalpostBuilder baseJournalpostBuilder() {
		return Journalpost.builder()
				.datoJournal(mapToLocalDateTime(DATO_JOURNAL))
				.datoDokument(mapToLocalDateTime(DATO_DOKUMENT))
				.datoEndret(mapToLocalDateTime(DATO_ENDRET))
				.dok(Collections.singletonList(DokumentInfo.builder()
						.relTilknyttetSom("HOVEDDOKUMENT")
						.datoFerdig(mapToLocalDateTime(DATO_DOKUMENT_FERDIG))
						.build()));
	}

	private LocalDateTime mapToLocalDateTime(final LocalDate date) {
		return LocalDateTime.from(date.atStartOfDay(ZoneId.of("Europe/Oslo")));
	}
}