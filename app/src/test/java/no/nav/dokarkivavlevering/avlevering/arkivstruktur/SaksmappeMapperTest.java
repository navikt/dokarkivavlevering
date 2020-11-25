package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled
class SaksmappeMapperTest {

	private SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper();

	@Test
	void shouldMap() throws Exception {
		SystemID sakSystemID = new SystemID();
		sakSystemID.setValue(UUID.randomUUID().toString());

		Journalpost jp = generateJournalPost();
		DokumentInfo dokInfo = generateDokumentInfo();
		dokInfo.getFildetaljer().add(generateFilDetaljer());
		jp.getDokumenter().add(dokInfo);
		Sak sak = generateSak();
		sak.getJournalposter().add(jp);

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		//saksmappe
		assertEquals(saksmappe.getSaksaar().toString(), "2019");
		assertEquals(saksmappe.getSakssekvensnummer().toString(), "1234567011");
		assertTrue(saksmappe.getSaksdato().equals(toGregorianCalendar("2019-10-28T10:41:36Z")));
		assertEquals(saksmappe.getAdministrativEnhet(), "NAV Medlemskap og avgift");
		assertEquals(saksmappe.getSaksansvarlig(), "Automatisk jobb");
		assertEquals(saksmappe.getSaksstatus(), "Under behandling");
		assertFalse(saksmappe.getSystemID().getValue().isEmpty());
		assertEquals(saksmappe.getMappeID(), "1234567011");
		assertEquals(saksmappe.getTittel(), "Medlemskap");
		assertTrue(saksmappe.getOpprettetDato().equals(toGregorianCalendar("2019-10-28T10:41:36Z")));
		assertEquals(saksmappe.getOpprettetAv(), "Automatisk jobb");
		assertEquals(saksmappe.getReferanseArkivdels().size(), 1);
		assertEquals(saksmappe.getParts().size(), 1);
		assertEquals(saksmappe.getRegistrerings().size(), 1);

		//saksmappe/referansearkivdels
		String referanseArkivdel = saksmappe.getReferanseArkivdels().get(0);
//		assertTrue(referanseArkivdel.equals(sak.getUuid().toString())); FIXME

		//saksmappe/part
		Part part = saksmappe.getParts().get(0);
		assertTrue(part.getPartID().equals("12345678911"));
		assertTrue(part.getPartNavn().equals("Frank"));
		assertTrue(part.getPartRolle().equals("Bruker"));

		//saksmappe/registrerings/registrering(journalpost)
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost reg = (no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0);
		assertTrue(reg.getJournalaar().toString().equals("2020"));
		assertTrue(reg.getJournalsekvensnummer().toString().equals("453637481"));
		assertTrue(reg.getJournalpostnummer().toString().equals("453637481"));
		assertTrue(reg.getJournalposttype().equals("Utgående dokument"));
		assertTrue(reg.getJournalstatus().equals("Arkivert"));
		assertTrue(reg.getJournaldato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertTrue(reg.getDokumentetsDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertTrue(!reg.getSystemID().getValue().isEmpty());
		assertTrue(reg.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertTrue(reg.getOpprettetAv().equals("srvmelosys"));
		assertTrue(reg.getRegistreringsID().equals("453637481"));
		assertTrue(reg.getTittel().equals("Legg til ny institusjon"));
		assertTrue(reg.getKorrespondanseparts().size() == 1);
		assertTrue(reg.getDokumentbeskrivelses().size() == 1);

		//saksmappe/registrerings/registrering(journalpost)/dokumentbeskrivelse
		Dokumentbeskrivelse dok = reg.getDokumentbeskrivelses().get(0);
		assertTrue(!dok.getSystemID().getValue().isEmpty());
		assertTrue(dok.getDokumenttype().equals("SED"));
		assertTrue(dok.getDokumentstatus().equals("FERDIGSTILT"));
		assertTrue(dok.getTittel().equals("Legg til ny institusjon"));
		assertTrue(dok.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
//		assertTrue(dok.getOpprettetAv().equals("systembruker")); FIXME
		assertTrue(dok.getTilknyttetRegistreringSom().equals("HOVEDDOKUMENT"));
		assertTrue(dok.getDokumentnummer().equals(toBigInteger(454017976)));
		assertTrue(dok.getTilknyttetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
//		assertTrue(dok.getTilknyttetAv().equals("srvmelosys")); FIXME
		assertTrue(dok.getDokumentobjekts().size() == 1);

		//saksmappe/registrerings/registrering(journalpost)/dokumentbeskrivelse/dokumentObjekts
		Dokumentobjekt dokObjekt = dok.getDokumentobjekts().get(0);
		assertTrue(!dokObjekt.getSystemID().getValue().isEmpty());
		assertTrue(dokObjekt.getVersjonsnummer().equals(toBigInteger(1)));
		assertTrue(dokObjekt.getVariantformat().equals("Arkivformat"));
		assertTrue(dokObjekt.getFormat().equals("PDF/A"));
		assertTrue(dokObjekt.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
//		assertTrue(dokObjekt.getOpprettetAv().equals("srvRuting")); FIXME
		//TODO: fix riktig
		assertTrue(dokObjekt.getReferanseDokumentfil().equals("URN til dokumentet i avleveringspakken (filnavn = DO + T_FIL_DETALJER.FIL_DETALJER_ID"));
		assertTrue(dokObjekt.getSjekksum().equals("TODO Sett sjekksum her"));
		assertTrue(dokObjekt.getSjekksumAlgoritme().equals("SHA-256"));
		assertTrue(dokObjekt.getFilstoerrelse().equals(toBigInteger(-1)));

		//saksmappe/registrerings/registrering(journalpost)/korrespondanseparts
		Korrespondansepart korrPart = reg.getKorrespondanseparts().get(0);
		assertTrue(korrPart.getKorrespondanseparttype().equals("Mottaker"));
		assertTrue(korrPart.getKorrespondansepartNavn().equals("srvmelosys"));
		assertTrue(korrPart.getSaksbehandler().equals("srvmelosys"));

	}

	private BigInteger toBigInteger(int smallInteger) {
		return new BigInteger(String.valueOf(smallInteger));
	}

	private Sak generateSak() throws Exception {
		return Sak.builder()
				.id((long) 1234567011)
				.tema("MED")
				.bruker(generaterBruker())
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(formatter.parse("2019-10-28 11:41:36.673"))
				.journalposter(new ArrayList<Journalpost>()).build();
	}

	private Bruker generaterBruker() {
		return new Bruker(
				"12345678911",
				"Frank"
		);
	}

	private Journalpost generateJournalPost() throws ParseException {
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
				.dokumenter(new ArrayList<DokumentInfo>())
				.arkivendringer(new ArrayList<Arkivendring>())
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
				.fildetaljer(new ArrayList<FilDetaljer>())
				.arkivendringer(new ArrayList<Arkivendring>())
				.build();
	}

	private FilDetaljer generateFilDetaljer() throws Exception {
		return FilDetaljer.builder()
				.id((long) 539876247)
				.filUuid("55c39cdb-f052-4f4e-a9a5-900b455ca915")
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.343"))
				.opprettetAv("srvRuting")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.build();
	}

	private XMLGregorianCalendar toGregorianCalendar(String date) throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
	}

}