package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.Utils.Utils;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SaksmappeMapper {

	public Saksmappe map(Sak sak, SystemID systemID) throws DatatypeConfigurationException {
		Saksmappe mappe = new Saksmappe();
		mappe.setSystemID(systemID);
		mappe.setOpprettetDato(dateToXMLGregorianCalendar(sak.getOpprettetTidspunkt()));
		//TODO: er sak.opprettet av et navn? if not må det fixes som skrevet i https://confluence.adeo.no/display/BOA/arkivstruktur.xml
		//TODO: hent navn for saksbehandler med id = SAK.OPPRETTET_AV og sett navn
		mappe.setOpprettetAv(mapOpprettetAv(sak.getOpprettetAv()));
		//TODO: Dene skal være "T_K_FAGOMRADE.DEKODE". Hentes dette fra db eller skal jeg mappe over? Ref kommentaren i Utils
		mappe.setTittel(sak.getTema());
		mappe.getReferanseArkivdels().add(systemID.toString());
		mappe.getParts().add(mapPart(sak.getBruker(), sak));
		mappe.setSaksaar(toBigInteger(sak.getOpprettetTidspunkt().getYear()));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setSaksdato(dateToXMLGregorianCalendar(sak.getOpprettetTidspunkt()));
		mappe.setAdministrativEnhet(Utils.getAdministrativEnhetFromTema(sak.getTema()));
		mappe.setSaksansvarlig(getSaksAnsvarlig(sak.getJournalposter()));
		mappe.setSaksstatus("Under behandling");
		for (Journalpost journalpost : sak.getJournalposter()) {
			mappe.getRegistrerings().add(mapRegistrering(journalpost, systemID));
		}
		return mappe;
	}

	private Part mapPart(Bruker bruker, Sak sak) {
		Part part = new Part();
		part.setPartRolle("Bruker");
		part.setPartID(determineSakID(bruker.getId(), sak));
		part.setPartNavn(determinePartNavn(bruker.getId(), sak));
		return part;
	}

	private Registrering mapRegistrering(Journalpost journalpost, SystemID systemID) throws DatatypeConfigurationException {
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering = new no.arkivverket.standarder.noark5.arkivstruktur.Journalpost();
		registrering.setSystemID(systemID);
		registrering.setOpprettetDato(dateToXMLGregorianCalendar(journalpost.getDatoOpprettet()));
		registrering.setOpprettetAv(journalpost.getOpprettetAvNavn());
		registrering.setRegistreringsID(journalpost.getId().toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalaar(toBigInteger(journalpost.getDatoJournal().getYear()));
		registrering.setJournalsekvensnummer(toBigInteger(journalpost.getId()));
		registrering.setJournalpostnummer(toBigInteger(journalpost.getId()));
		registrering.setJournalposttype(determineJournalPostType(journalpost.getType()));
		registrering.setJournaldato(dateToXMLGregorianCalendar(journalpost.getDatoJournal()));
		registrering.setSendtDato(dateToXMLGregorianCalendar(determineSendtDato(journalpost)));
		registrering.setRegistreringsID(journalpost.getId().toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalstatus("Arkivert");

		//Skal kun settes hvis det er et notat.
		//TODO: er det slik at alt som ikke er "N" er notater?
		if (!"N".equals(journalpost.getType())) {
			registrering.getKorrespondanseparts().add(mapKorrespondansePart(journalpost));
		}
		//Skal kun settes om journalpost.getDatoDokument() != null
		if (journalpost.getDatoDokument() != null) {
			registrering.setDokumentetsDato(dateToXMLGregorianCalendar(journalpost.getDatoDokument()));
		}
		//Skal kun settes om journalpost.getDatoDokument() != null && journalpostType == "I"
		if ("I".equals(journalpost.getType()) && journalpost.getDatoMottatt() != null) {
			registrering.setMottattDato(dateToXMLGregorianCalendar(journalpost.getDatoDokument()));
		}
		for (DokumentInfo dokumentInfo : journalpost.getDokumenter()) {
			registrering.getDokumentbeskrivelses().add(mapDokumentBeskrivelse(dokumentInfo, systemID));
		}

		return registrering;
	}


	private Korrespondansepart mapKorrespondansePart(Journalpost journalpost) {
		Korrespondansepart korrespondansepart = new Korrespondansepart();
		korrespondansepart.setKorrespondanseparttype(mapKorrespondanseParttype(journalpost.getType()));
		korrespondansepart.setKorrespondansepartNavn(journalpost.getOpprettetAvNavn());
		korrespondansepart.setSaksbehandler(journalpost.getOpprettetAvNavn());
		return korrespondansepart;
	}

	private Dokumentbeskrivelse mapDokumentBeskrivelse(DokumentInfo dokumentInfo, SystemID systemID) throws DatatypeConfigurationException {
		Dokumentbeskrivelse dokumentbeskrivelse = new Dokumentbeskrivelse();
		dokumentbeskrivelse.setSystemID(systemID);
		//TODO: add decode
		dokumentbeskrivelse.setDokumenttype(dokumentInfo.getKategori());
		//TODO: add decode
		dokumentbeskrivelse.setDokumentstatus(dokumentInfo.getStatus());
		dokumentbeskrivelse.setTittel(dokumentInfo.getTittel());
		dokumentbeskrivelse.setOpprettetDato(dateToXMLGregorianCalendar(dokumentInfo.getDatoOpprettet()));
		dokumentbeskrivelse.setOpprettetAv(mapOpprettetAv(dokumentInfo.getOpprettetAv()));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(dokumentInfo.getRelasjonTilknyttetSom());
		dokumentbeskrivelse.setDokumentnummer(toBigInteger(dokumentInfo.getId()));
		dokumentbeskrivelse.setTilknyttetDato(dateToXMLGregorianCalendar(dokumentInfo.getRelasjonDatoOpprettet()));
		dokumentbeskrivelse.setTilknyttetAv(dokumentInfo.getOpprettetAv());

		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljer()) {
			dokumentbeskrivelse.getDokumentobjekts().add(mapDokumentobjekt(filDetaljer, systemID));
		}
		return dokumentbeskrivelse;
	}

	private Dokumentobjekt mapDokumentobjekt(FilDetaljer filDetaljer, SystemID systemID) throws DatatypeConfigurationException {
		Dokumentobjekt dokumentobjekt = new Dokumentobjekt();
		dokumentobjekt.setSystemID(systemID);
		dokumentobjekt.setVersjonsnummer(toBigInteger(1));
		dokumentobjekt.setVariantformat("Arkivformat");
		dokumentobjekt.setFormat("PDF/A");
		dokumentobjekt.setOpprettetDato(dateToXMLGregorianCalendar(filDetaljer.getDatoOpprettet()));
		dokumentobjekt.setOpprettetAv(filDetaljer.getOpprettetAv());
		//TODO: Fix filpath
		dokumentobjekt.setReferanseDokumentfil("URN til dokumentet i avleveringspakken (filnavn = DO + T_FIL_DETALJER.FIL_DETALJER_ID");
		//TODO: checksum
		dokumentobjekt.setSjekksum("TODO Sett sjekksum her");
		dokumentobjekt.setSjekksumAlgoritme("SHA-256");
		//TODO: Finn filstørrelse
		dokumentobjekt.setFilstoerrelse(toBigInteger(-1));

		return dokumentobjekt;
	}

	private String getSaksAnsvarlig(List<Journalpost> journalposter) {
		Journalpost journalpost = journalposter.stream().min(Comparator.comparing(Journalpost::getId)).orElseThrow(NoSuchElementException::new);
		return mapEndretAv(journalpost.getEndretAv());
	}

	private String determinePartNavn(String brukerID, Sak sak) {
		//TODO: er det riktig at orgNr alltid er 9 i length? AktørID er hvor langt?
		//TODO: Integrer med PDL
		//TODO: Integrer med aktoerregister

		return sak.getOpprettetAv().length() == 9 ? "hent organisasjonens navn fra Enhetsregisteret" : "hent fnr fra aktørregister";
	}

	private String determineSakID(String brukerID, Sak sak) {
		//TODO: er det riktig at orgNr alltid er 9 i length? AktørID er hvor langt?
		//TODO: hent fnr fra aktoerregister
		return sak.getOpprettetAv().length() == 9 ? sak.getOpprettetAv() : "hent fnr fra aktørregister";
	}


	private String determineJournalPostType(String journalpostType) {
		switch (journalpostType) {
			case "U":
				return "Utgående dokument";
			case "I":
				return "Inngående dokument";
			default:
				return "Organinternt dokument uten oppfølging";
		}
	}

	private Date determineSendtDato(Journalpost journalpost) {
		switch (journalpost.getStatus()) {
			case "E":
				if (journalpost.getDatoEkspedert() != null)
					return journalpost.getDatoEkspedert();
			case "FS":
				if (journalpost.getDatoSendtPrint() != null)
					return journalpost.getDatoSendtPrint();
			default:
				return journalpost.getDatoJournal();
		}
	}

	private BigInteger toBigInteger(int smallInteger) {
		return new BigInteger(String.valueOf(smallInteger));
	}

	private BigInteger toBigInteger(long smallLong) {
		return new BigInteger(String.valueOf(smallLong));
	}

	private String mapKorrespondanseParttype(String journalpost_t) {
		return "I".equalsIgnoreCase(journalpost_t) ? "Avsender" : "Mottaker";
	}

	private boolean isSystembruker(String bruker) {
		//TODO: Hvordan finne ut av om det er snakk om en systembruker?
		return bruker.contains("srv") ? true : false;
	}

	private String mapEndretAv(String endretAv) {
		return isSystembruker(endretAv) ? "Automatisk jobb" : endretAv;
	}

	private String mapOpprettetAv(String opprettetAv) {
		return isSystembruker(opprettetAv) ? "systembruker" : opprettetAv;
	}

	private XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws DatatypeConfigurationException {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toInstant().toString());
	}

}
