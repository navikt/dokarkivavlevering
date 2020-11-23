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
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class SaksmappeMapperTest {

	private SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");


	private enum journalpostType {
		U, I, N
	}

	private enum journalpostStatus {
		FS, J, FL
	}

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
		assertThat(saksmappe.getSaksaar() == toBigInteger(2019));
		assertThat(saksmappe.getSakssekvensnummer() == toBigInteger(1234567011));
		assertThat(saksmappe.getSaksdato().equals(toGregorianCalendar("2019-10-28T10:41:36Z")));
		assertThat(saksmappe.getAdministrativEnhet().equals("NAV Medlemskap og avgift"));
		assertThat(saksmappe.getSaksansvarlig().equals("Automatisk jobb"));
		assertThat(saksmappe.getSaksstatus().equals("Under behandling"));
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
		assertThat(saksmappe.getMappeID().equals(1234567011));
		assertThat(saksmappe.getTittel().equals("Medlemskal"));
		assertThat(saksmappe.getOpprettetDato().equals(toGregorianCalendar("2019-10-28T10:41:36Z")));
		assertThat(saksmappe.getOpprettetAv().equals("systembruker"));
		assertThat(saksmappe.getReferanseArkivdels().size() == 1);
		assertThat(saksmappe.getParts().size() == 1);
		assertThat(saksmappe.getRegistrerings().size() == 1);

		//saksmappe/referansearkivdels
		String referanseArkivdel = saksmappe.getReferanseArkivdels().get(0);
		assertThat(referanseArkivdel.equals(sak.getUuid().toString()));

		//saksmappe/part
		Part part = saksmappe.getParts().get(0);
		assertThat(part.getPartID().equals("hent fnr fra aktørregister"));
		assertThat(part.getPartNavn().equals("Hent navn fra PDL"));
		assertThat(part.getPartRolle().equals("Bruker"));

		//saksmappe/registrerings/registrering(journalpost)
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost reg = (no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0);
		assertThat(reg.getJournalaar().equals("2020"));
		assertThat(reg.getJournalsekvensnummer().equals("453637481"));
		assertThat(reg.getJournalpostnummer().equals(toBigInteger(453637481)));
		assertThat(reg.getJournalposttype().equals("Utgående dokument"));
		assertThat(reg.getJournalstatus().equals("Arkivert"));
		assertThat(reg.getJournaldato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(reg.getDokumentetsDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(!reg.getSystemID().getValue().isEmpty());
		assertThat(reg.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(reg.getOpprettetAv().equals("srvmelosys"));
		assertThat(reg.getRegistreringsID().equals("453637481"));
		assertThat(reg.getTittel().equals("Legg til ny institusjon"));
		assertThat(reg.getKorrespondanseparts().size() == 1);
		assertThat(reg.getDokumentbeskrivelses().size() == 1);

		//saksmappe/registrerings/registrering(journalpost)/dokumentbeskrivelse
		Dokumentbeskrivelse dok = reg.getDokumentbeskrivelses().get(0);
		assertThat(!dok.getSystemID().getValue().isEmpty());
		assertThat(dok.getDokumenttype().equals("SED"));
		assertThat(dok.getDokumentstatus().equals("FERDIGSTILT"));
		assertThat(dok.getTittel().equals("Legg til ny institusjon"));
		assertThat(dok.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(dok.getOpprettetAv().equals("systembruker"));
		assertThat(dok.getTilknyttetRegistreringSom().equals("HOVEDDOKUMENT"));
		assertThat(dok.getDokumentnummer().equals(toBigInteger(454017976)));
		assertThat(dok.getTilknyttetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(dok.getTilknyttetAv().equals("srvmelosys"));
		assertThat(dok.getDokumentobjekts().size() == 1);

		//saksmappe/registrerings/registrering(journalpost)/dokumentbeskrivelse/dokumentObjekts
		Dokumentobjekt dokObjekt = dok.getDokumentobjekts().get(0);
		assertThat(!dokObjekt.getSystemID().getValue().isEmpty());
		assertThat(dokObjekt.getVersjonsnummer().equals(toBigInteger(1)));
		assertThat(dokObjekt.getVariantformat().equals("Arkivformat"));
		assertThat(dokObjekt.getFormat().equals("PDF/A"));
		assertThat(dokObjekt.getOpprettetDato().equals(toGregorianCalendar("2020-11-10T15:04:43Z")));
		assertThat(dokObjekt.getOpprettetAv().equals("srvRuting"));
		//TODO: fix riktig
		assertThat(dokObjekt.getReferanseDokumentfil().equals("URN til dokumentet i avleveringspakken (filnavn = DO + T_FIL_DETALJER.FIL_DETALJER_ID"));
		assertThat(dokObjekt.getSjekksum().equals("TODO Sett sjekksum her"));
		assertThat(dokObjekt.getSjekksumAlgoritme().equals("SHA-256"));
		assertThat(dokObjekt.getFilstoerrelse().equals(toBigInteger(-1)));

		//saksmappe/registrerings/registrering(journalpost)/korrespondanseparts
		Korrespondansepart korrPart = reg.getKorrespondanseparts().get(0);
		assertThat(korrPart.getKorrespondanseparttype().equals("Mottaker"));
		assertThat(korrPart.getKorrespondansepartNavn().equals("srvmelosys"));
		assertThat(korrPart.getSaksbehandler().equals("srvmelosys"));

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
				"00000000000",
				//TODO: Rydd opp når enrichment er satt oppp
				"Dette skal mappes utenifra."
		);
	}

	private Journalpost generateJournalPost() throws ParseException {
		return new Journalpost(
				(long) 453637481,
				journalpostType.U.toString(),
				journalpostStatus.FS.toString(),
				"Legg til ny institusjon",
				"Arena",
				null,
				formatter.parse("2020-11-10 16:04:43.332"),
				formatter.parse("2020-11-10 16:04:43.35"),
				formatter.parse("2020-11-10 16:04:43.338"),
				null,
				null,
				"srvmelosys",
				"srvmelosys",
				"srvmelosys",
				new ArrayList<DokumentInfo>(),
				new ArrayList<Arkivendring>()
		);
	}

	private DokumentInfo generateDokumentInfo() throws Exception {
		return new DokumentInfo(
				(long) 454017976,
				"HOVEDDOKUMENT",
				formatter.parse("2020-11-10 16:04:43.343"),
				"srvmelosys",
				"SED",
				"FERDIGSTILT",
				"Legg til ny institusjon",
				formatter.parse("2020-11-10 16:04:43.342"),
				"srvmelosys",
				new ArrayList<FilDetaljer>(),
				new ArrayList<Arkivendring>()
		);
	}

	private FilDetaljer generateFilDetaljer() throws Exception {
		return new FilDetaljer(
				(long) 539876247,
				"55c39cdb-f052-4f4e-a9a5-900b455ca915",
				formatter.parse("2020-11-10 16:04:43.343"),
				"srvRuting"
		);
	}

	private XMLGregorianCalendar toGregorianCalendar(String date) throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
	}


}