package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.Klasse;
import no.arkivverket.standarder.noark5.offentligjournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.offentligjournal.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OffentligJournalRegistreringMapperTest {
	private SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
	private OffentligJournalRegistreringMapper mapper = new OffentligJournalRegistreringMapper();

	@Test
	void testMapping() throws Exception {
		final Journalregistrering journalRegistrering = mapper.map(generateSak(), generateSak().getJournalposter().get(0));

		assertKlasse(journalRegistrering.getKlasse());
		assertMappe(journalRegistrering.getSaksmappe());
		assertJournalpost(journalRegistrering.getJournalpost());
	}

	private void assertKlasse(Klasse klasse) {
		assertEquals(klasse.getKlasseID(), "MED");
		assertEquals(klasse.getTittel(), "Medlemskap");
	}

	private void assertJournalpost(no.arkivverket.standarder.noark5.offentligjournal.Journalpost jp) throws Exception {
		assertEquals(jp.getSystemID().getValue().isEmpty(), false);
		assertEquals(jp.getJournalaar(), toBigInteger(2020));
		assertEquals(jp.getJournalsekvensnummer(), toBigInteger(453637481));
		assertEquals(jp.getJournalpostnummer(), toBigInteger(453637481));
		assertEquals(jp.getOffentligTittel(), "Legg til ny institusjon");
		assertEquals(jp.getJournaldato(), toGregorianCalendar("2020-11-10T15:04:43.000Z"));
		assertEquals(jp.getDokumentetsDato(), toGregorianCalendar("2020-11-10T15:04:43.000Z"));
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

	private XMLGregorianCalendar toGregorianCalendar(String date) throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
	}

	private Sak generateSak() throws Exception {
		return Sak.builder()
				.id((long) 1234567011)
				.tema("MED")
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(formatter.parse("2019-10-28 11:41:36.673"))
				.journalposter(Arrays.asList(generateJournalPost())).build();
	}

	private Journalpost generateJournalPost() throws Exception {
		return Journalpost.builder()
				.id((long) 453637481)
				.type("U")
				.status("FS")
				.innhold("Legg til ny institusjon")
				.avsenderMottaker("Arena")
				.datoMottatt(null)
				.datoDokument(formatter.parse("2020-11-10 16:04:43.332"))
				.datoJournal(formatter.parse("2020-11-10 16:04:43.35"))
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.338"))
				.datoEkspedert(null)
				.datoSendtPrint(null)
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.opprettetAvNavn("srvmelosys")
				.endretAv("srvmelosys")
				.endretAvBeriketNavn(null)
				.dokumenter(Arrays.asList(generateDokumentInfo()))
				.build();
	}

	private DokumentInfo generateDokumentInfo() throws Exception {
		return DokumentInfo.builder()
				.id((long) 454017976)
				.relasjonTilknyttetSom("HOVEDDOKUMENT")
				.relasjonDatoOpprettet(formatter.parse("2020-11-10 16:04:43.343"))
				.relasjonOpprettetAv("srvmelosys")
				.relasjonOpprettetAvBeriketNavn("Automatisk Jobb")
				.kategori("SED")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.build();
	}

}