package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.Klasse;
import no.arkivverket.standarder.noark5.loependejournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.loependejournal.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
class LoependeJournalRegistreringMapperTest {
	private SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
	private JournalRegistreringMapper mapper = new JournalRegistreringMapper();

	@Test
	void testMapping() throws Exception {
		final Sak sak = generateSak();
		final Journalregistrering registrering = mapper.map(sak, sak.getJp().get(0));

		Klasse k = registrering.getKlasse();
		assertEquals(k.getKlasseID(), "MED");
		assertEquals(k.getTittel(), "Medlemskap");

		assertMappe(registrering.getSaksmappe());
		assertJournalpost(registrering.getJournalpost());


		Saksmappe mappe = registrering.getSaksmappe();
		assertEquals(mappe.getSaksaar(), toBigInteger(2019));
		assertEquals(mappe.getSakssekvensnummer(), toBigInteger((1234567011)));
		assertEquals(mappe.getTittel(), "Medlemskap");


		no.arkivverket.standarder.noark5.loependejournal.Journalpost jp = registrering.getJournalpost();
		assertEquals(jp.getSystemID().getValue().isEmpty(), false);
		assertEquals(jp.getJournalaar(), toBigInteger(2020));
		assertEquals(jp.getTittel(), "Legg til ny institusjon");
		assertEquals(jp.getJournaldato(), toGregorianCalendar("2020-11-10T15:04:43Z"));
		assertEquals(jp.getDokumentetsDato(), toGregorianCalendar("2020-11-10T15:04:43Z"));

		Korrespondansepart part = jp.getKorrespondanseparts().get(0);
		assertEquals(part.getKorrespondansepartNavn(), "Arena");
		assertEquals(part.getKorrespondanseparttype(), "Mottaker");


	}

	private void assertJournalpost(no.arkivverket.standarder.noark5.loependejournal.Journalpost jp) throws Exception {
		assertEquals(jp.getSystemID().getValue().isEmpty(), false);
		assertEquals(jp.getJournalaar(), toBigInteger(2020));
		assertEquals(jp.getTittel(), "Legg til ny institusjon");
		assertEquals(jp.getJournalsekvensnummer(), toBigInteger(453637481));
		assertEquals(jp.getJournalpostnummer(), toBigInteger(453637481));
		assertEquals(jp.getJournaldato(), toGregorianCalendar("2020-11-10T15:04:43Z"));
		assertEquals(jp.getDokumentetsDato(), toGregorianCalendar("2020-11-10T15:04:43Z"));
		assertKorrespondanseParts(jp.getKorrespondanseparts().get(0));
	}

	private void assertKorrespondanseParts(Korrespondansepart part){
		assertEquals(part.getKorrespondansepartNavn(), "Arena");
		assertEquals(part.getKorrespondanseparttype(), "Mottaker");
	}

	private void assertMappe(Saksmappe mappe) {
		assertEquals(mappe.getSaksaar(), toBigInteger(2019));
		assertEquals(mappe.getSakssekvensnummer(), toBigInteger(1234567011));
		assertEquals(mappe.getTittel(), "Medlemskap");
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
				.jp(Arrays.asList(generateJournalPost())).build();
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
				.dok(Arrays.asList(generateDokumentInfo()))
				.build();
	}

	private DokumentInfo generateDokumentInfo() throws Exception {
		return DokumentInfo.builder()
				.id((long) 454017976)
				.relTilknyttetSom("HOVEDDOKUMENT")
				.relDatoOpprettet(formatter.parse("2020-11-10 16:04:43.343"))
				.relOpprettetAv("srvmelosys")
				.relOpprettetAvBeriketNavn("Automatisk Jobb")
				.kategori("SED")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.build();
	}

}