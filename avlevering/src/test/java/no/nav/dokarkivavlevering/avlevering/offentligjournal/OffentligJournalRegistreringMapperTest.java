package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.Klasse;
import no.arkivverket.standarder.noark5.offentligjournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.offentligjournal.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.toLocalDateTime;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OffentligJournalRegistreringMapperTest {
	private final OffentligJournalRegistreringMapper mapper = new OffentligJournalRegistreringMapper(new JournaldatoMapper());

	@Test
	void testMapping() throws Exception {
		final Journalregistrering journalRegistrering = mapper.map(generateSak(), generateSak().getJp().get(0));

		assertKlasse(journalRegistrering.getKlasse());
		assertMappe(journalRegistrering.getSaksmappe());
		assertJournalpost(journalRegistrering.getJournalpost());
	}

	private void assertKlasse(Klasse klasse) {
		assertEquals(klasse.getKlasseID(), "MED");
		assertEquals(klasse.getTittel(), "Medlemskap");
	}

	private void assertJournalpost(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) throws Exception {
		assertThat(jp.getSystemID().getValue()).isNotEmpty();
		assertEquals(jp.getJournalaar(), toBigInteger(2020));
		assertEquals(jp.getJournalsekvensnummer(), toBigInteger(453637481));
		assertEquals(jp.getJournalpostnummer(), toBigInteger(453637481));
		assertEquals(jp.getOffentligTittel(), "Legg til ny institusjon");
		assertEquals(jp.getJournaldato(), TestUtils.toLocalDateTime("2020-11-10 16:04:43").toLocalDate());
		assertEquals(jp.getDokumentetsDato(), TestUtils.toLocalDateTime("2020-11-10 16:05:43").toLocalDate());
		assertEquals(jp.getSkjermingMetadata(), "Skjerming navn mottaker");
		assertEquals(jp.getSkjermingshjemmel(), "Offentleglova § 13");
		assertKorrespondanseParts(jp.getKorrespondanseparts().get(0));
	}

	private void assertKorrespondanseParts(Korrespondansepart part) {
		assertEquals(part.getKorrespondansepartNavn(), "****");
		assertEquals(part.getKorrespondanseparttype(), "Mottaker");
	}

	private void assertMappe(Saksmappe mappe) {
		assertEquals(mappe.getSaksaar(), toBigInteger(2019));
		assertEquals(mappe.getSakssekvensnummer(), toBigInteger((1234567011)));
		assertEquals(mappe.getOffentligTittel(), "Medlemskap");
	}

	private Sak generateSak() throws Exception {
		return Sak.builder()
				.id((long) 1234567011)
				.tema("MED")
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(toLocalDateTime("2019-10-28 11:41:36.673"))
				.jp(singletonList(generateJournalPost())).build();
	}

	private Journalpost generateJournalPost() throws Exception {
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
				.dok(singletonList(generateDokumentInfo()))
				.build();
	}

	private DokumentInfo generateDokumentInfo() throws Exception {
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