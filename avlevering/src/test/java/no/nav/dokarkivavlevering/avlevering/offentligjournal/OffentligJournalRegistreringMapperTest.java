package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.Klasse;
import no.arkivverket.standarder.noark5.offentligjournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.offentligjournal.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.MED;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.PER;
import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.toLocalDateTime;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OffentligJournalRegistreringMapperTest {
	private final OffentligJournalRegistreringMapper mapper = new OffentligJournalRegistreringMapper(new JournaldatoMapper());
	private static final String NOTAT = "N";

	@Test
	void shouldMapOffentligJournal() {
		Sak sak = generateSak();
		final Journalregistrering journalRegistrering = mapper.map(generateSak(), sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), MED);
		assertMappe(journalRegistrering.getSaksmappe(), MED);
		assertJournalpostWithSkjermingMetadata(journalRegistrering.getJournalpost());
	}

	@Test
	void shouldMapOffentligJournalForPer() {
		Sak sak = generateSak();
		final Journalregistrering journalRegistrering = mapper.map(generateSak(PER.name()), sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), PER);
		assertMappe(journalRegistrering.getSaksmappe(), PER);
		assertJournalpostWithSkjermingMetadataTemaPER(journalRegistrering.getJournalpost());
	}

	@Test
	void shouldMapOffentligJournalForOffentligJournalAvsenderMottaker() {
		Sak sak = generateSakWithoutJournalposts(MED.name());
		Journalpost jp = generateJournalPost().offentligJournalAvsenderMottaker("Navn Navnesen").build();
		sak.getJp().add(jp);

		final Journalregistrering journalRegistrering = mapper.map(sak, sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), MED);
		assertMappe(journalRegistrering.getSaksmappe(), MED);
		assertJournalpostWithoutSkjermingMetadata(journalRegistrering.getJournalpost());
	}

	@ParameterizedTest
	@CsvSource({
			"123",
			"12345678",
			"123456789",
			"1234567890123"
	})
	void shouldMapOffentligJournalForMottakerWith3or8or9or13numbers(String idnr) {
		Sak sak = generateSakWithoutJournalposts(MED.name());
		Journalpost jp = generateJournalPost().avsenderMottakerId(idnr).build();
		sak.getJp().add(jp);

		final Journalregistrering journalRegistrering = mapper.map(sak, sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), MED);
		assertMappe(journalRegistrering.getSaksmappe(), MED);
		assertJournalpostWithoutSkjermingMetadata(journalRegistrering.getJournalpost());
	}

	@ParameterizedTest
	@CsvSource({
			"HPRNR",
			"UTL_ORG"
	})
	void shouldMapOffentligJournalForHPRNR_UTL_ORG(String idtype) {
		Sak sak = generateSakWithoutJournalposts(MED.name());
		Journalpost jp = generateJournalPost().avsenderMottakerIdType(idtype).build();
		sak.getJp().add(jp);

		final Journalregistrering journalRegistrering = mapper.map(sak, sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), MED);
		assertMappe(journalRegistrering.getSaksmappe(), MED);
		assertJournalpostWithoutSkjermingMetadata(journalRegistrering.getJournalpost());
	}

	@Test
	void shouldNotSkjermeNotat() {
		Sak sak = generateSakWithoutJournalposts(MED.name());
		Journalpost jp = generateJournalPost().type(NOTAT).build();
		sak.getJp().add(jp);

		final Journalregistrering journalRegistrering = mapper.map(sak, sak.getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse(), MED);
		assertMappe(journalRegistrering.getSaksmappe(), MED);
		assertJournalpostWithoutSkjermingMetadataAndKorrespondansepart(journalRegistrering.getJournalpost());
	}

	private void assertKlasse(Klasse klasse, Tema tema) {
		assertEquals(klasse.getKlasseID(), tema.getTemakode());
		assertEquals(klasse.getTittel(), tema.getTemanavn());
	}

	private void assertJournalpostWithSkjermingMetadata(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) {
		assertBaseJournalpost(jp);
		assertEquals(jp.getSkjermingMetadata(), "Skjerming navn mottaker");
		assertEquals(jp.getSkjermingshjemmel(), "Offl. § 13 1. ledd, jf fvl § 13 1. ledd nr. 1 / NAV-loven § 7");
		assertKorrespondanseParts(jp.getKorrespondanseparts().get(0));
	}

	private void assertJournalpostWithSkjermingMetadataTemaPER(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) {
		assertBaseJournalpost(jp);
		assertEquals(jp.getSkjermingMetadata(), "Skjerming navn mottaker");
		assertEquals(jp.getSkjermingshjemmel(), "Offl. § 13 1. ledd, jf fvl § 13 1. ledd nr. 2 / NAV-loven § 7");
		assertKorrespondanseParts(jp.getKorrespondanseparts().get(0));
	}

	private void assertJournalpostWithoutSkjermingMetadata(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) {
		assertBaseJournalpost(jp);
		assertNull(jp.getSkjermingMetadata());
		assertNull(jp.getSkjermingshjemmel());
		assertKorrespondanseParts(jp.getKorrespondanseparts().get(0), "Arena", "Mottaker");
	}

	private void assertJournalpostWithoutSkjermingMetadataAndKorrespondansepart(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) {
		assertBaseJournalpost(jp);
		assertNull(jp.getSkjermingMetadata());
		assertNull(jp.getSkjermingshjemmel());
		assertThat(jp.getKorrespondanseparts().isEmpty());
	}

	private void assertBaseJournalpost(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) {
		assertThat(jp.getSystemID().getValue()).isNotEmpty();
		assertEquals(jp.getJournalaar(), toBigInteger(2020));
		assertEquals(jp.getJournalsekvensnummer(), toBigInteger(453637481));
		assertEquals(jp.getJournalpostnummer(), toBigInteger(453637481));
		assertEquals(jp.getOffentligTittel(), "Legg til ny institusjon");
		assertEquals(jp.getJournaldato(), TestUtils.toLocalDateTime("2020-11-10 16:04:43").toLocalDate());
		assertEquals(jp.getDokumentetsDato(), TestUtils.toLocalDateTime("2020-11-10 16:05:43").toLocalDate());
	}

	private void assertKorrespondanseParts(Korrespondansepart part) {
		assertKorrespondanseParts(part, "****", "Mottaker");
	}

	private void assertKorrespondanseParts(Korrespondansepart part, String expectedName, String expectedType) {
		assertEquals(part.getKorrespondansepartNavn(), expectedName);
		assertEquals(part.getKorrespondanseparttype(), expectedType);
	}

	private void assertMappe(Saksmappe mappe, Tema tema) {
		assertEquals(mappe.getSaksaar(), toBigInteger(2019));
		assertEquals(mappe.getSakssekvensnummer(), toBigInteger((1234567011)));
		assertEquals(mappe.getOffentligTittel(), tema.getTemanavn());
	}

	private Sak generateSak() {
		return generateSak("MED");
	}

	private Sak generateSak(String tema) {
		Sak sak = generateSakWithoutJournalposts(tema);
		sak.getJp().add(generateJournalPost().build());
		return sak;
	}

	private Sak generateSakWithoutJournalposts(String tema) {
		return Sak.builder()
				.id((long) 1234567011)
				.tema(tema)
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(toLocalDateTime("2019-10-28 11:41:36.673"))
				.jp(new ArrayList<>()).build();
	}

	private Journalpost.JournalpostBuilder generateJournalPost() {
		return Journalpost.builder()
				.id((long) 453637481)
				.type("U")
				.status("FS")
				.innhold("Legg til ny institusjon")
				.avsenderMottaker("Arena")
				.datoMottatt(null)
				.datoDokument(toLocalDateTime("2020-11-10 16:05:43.332"))
				.datoJournal(toLocalDateTime("2020-11-10 16:04:43.35"))
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.338"))
				.datoEkspedert(null)
				.datoSendtPrint(null)
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.opprettetAvNavn("srvmelosys")
				.endretAv("srvmelosys")
				.endretAvBeriketNavn(null)
				.dok(singletonList(generateDokumentInfo()));
	}

	private DokumentInfo generateDokumentInfo() {
		return DokumentInfo.builder()
				.id((long) 454017976)
				.relTilknyttetSom("HOVEDDOKUMENT")
				.relDatoOpprettet(toLocalDateTime("2020-11-10 16:04:43.343"))
				.relOpprettetAv("srvmelosys")
				.relOpprettetAvBeriketNavn("Automatisk Jobb")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.build();
	}


}