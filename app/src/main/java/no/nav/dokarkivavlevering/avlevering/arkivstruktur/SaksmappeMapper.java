package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.dateToXMLGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SaksmappeMapper {

	private final AvleveringProperties.ArkivConfig arkivConfig;

	public SaksmappeMapper(AvleveringProperties avleveringProperties) {
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	public Saksmappe map(Sak sak) {
		Saksmappe mappe = new Saksmappe();
		mappe.setSystemID(mapSystemID(sak.getUuid()));
		mappe.setMappeID(sak.getId().toString());
		mappe.setOpprettetDato(dateToXMLGregorianCalendar(sak.getOpprettetTidspunkt()));
		mappe.setOpprettetAv(setSystembrukerOrBeriket(sak.getOpprettetAv(), sak.getOpprettetAvBeriketNavn()));
		mappe.setTittel(temaNavnDecode(sak.getTema()));
		mappe.getReferanseArkivdels().add(arkivConfig.getArkivdelConfig().getSystemID());
		mappe.getParts().add(mapPart(sak));
		mappe.setSaksaar(toBigInteger(getYear(sak.getOpprettetTidspunkt())));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setSaksdato(dateToXMLGregorianCalendar(sak.getOpprettetTidspunkt()));
		mappe.setAdministrativEnhet(getAdministrativEnhetFromTema((sak.getTema())));
		mappe.setSaksansvarlig(getSaksAnsvarlig(sak.getJp()));
		mappe.setSaksstatus("Under behandling");
		for (Journalpost journalpost : sak.getJp()) {
			mappe.getRegistrerings().add(mapRegistrering(journalpost, sak.getTema()));
		}
		return mappe;
	}

	private Part mapPart(Sak sak) {
		Part part = new Part();
		part.setPartRolle("Bruker");
		part.setPartID(determinePartID(sak));
		part.setPartNavn(sak.getBruker().getNavn());
		return part;
	}

	private Registrering mapRegistrering(Journalpost journalpost, String tema) {
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering = new no.arkivverket.standarder.noark5.arkivstruktur.Journalpost();
		registrering.setSystemID(mapSystemID(journalpost.getUuid()));
		registrering.setOpprettetDato(dateToXMLGregorianCalendar(journalpost.getDatoOpprettet()));
		registrering.setOpprettetAv(journalpost.getOpprettetAvNavn());
		registrering.setRegistreringsID(journalpost.getId().toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalaar(toBigInteger(getYear(journalpost.getDatoJournal())));
		registrering.setJournalsekvensnummer(toBigInteger(journalpost.getId()));
		registrering.setJournalpostnummer(toBigInteger(journalpost.getId()));
		registrering.setJournalposttype(determineJournalPostType(journalpost.getType()));
		registrering.setJournaldato(dateToXMLGregorianCalendar(journalpost.getDatoJournal()));
		registrering.setSendtDato(dateToXMLGregorianCalendar(determineSendtDato(journalpost)));
		registrering.setRegistreringsID(journalpost.getId().toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalstatus("Arkivert");

		//Skal kun settes hvis det ikke er et notat.
		if (!"N".equals(journalpost.getType())) {
			registrering.getKorrespondanseparts().add(mapKorrespondansePart(journalpost));
		}
		//Skal kun settes om journalpost.getDatoDokument() != null
		if (journalpost.getDatoDokument() != null) {
			registrering.setDokumentetsDato(dateToXMLGregorianCalendar(journalpost.getDatoDokument()));
		}
		//Skal kun settes om journalpost.getDatoMottatt() != null && journalpostType == "I"
		if ("I".equals(journalpost.getType()) && journalpost.getDatoMottatt() != null) {
			registrering.setMottattDato(dateToXMLGregorianCalendar(journalpost.getDatoMottatt()));
		}
		for (DokumentInfo dokumentInfo : journalpost.getDok()) {
			registrering.getDokumentbeskrivelses().add(mapDokumentBeskrivelse(dokumentInfo, tema));
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

	private Dokumentbeskrivelse mapDokumentBeskrivelse(DokumentInfo dokumentInfo, String tema) {
		Dokumentbeskrivelse dokumentbeskrivelse = new Dokumentbeskrivelse();
		dokumentbeskrivelse.setSystemID(mapSystemID(dokumentInfo.getUuid()));
		dokumentbeskrivelse.setDokumenttype(dokumentInfo.getKategori());
		dokumentbeskrivelse.setDokumentstatus(dokumentInfo.getStatus());
		dokumentbeskrivelse.setTittel(dokumentInfo.getTittel());
		dokumentbeskrivelse.setOpprettetDato(dateToXMLGregorianCalendar(dokumentInfo.getDatoOpprettet()));
		dokumentbeskrivelse.setOpprettetAv(setSystembrukerOrBeriket(dokumentInfo.getOpprettetAv(), dokumentInfo.getOpprettetAvBeriketNavn()));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(dokumentInfo.getRelTilknyttetSom());
		dokumentbeskrivelse.setDokumentnummer(toBigInteger(dokumentInfo.getId()));
		dokumentbeskrivelse.setTilknyttetDato(dateToXMLGregorianCalendar(dokumentInfo.getRelDatoOpprettet()));
		dokumentbeskrivelse.setTilknyttetAv(setSystembrukerOrBeriket(dokumentInfo.getOpprettetAv(), dokumentInfo.getOpprettetAvBeriketNavn()));

		for (FilDetaljer filDetaljer : dokumentInfo.getFd()) {
			dokumentbeskrivelse.getDokumentobjekts().add(mapDokumentobjekt(filDetaljer, tema));
		}
		return dokumentbeskrivelse;
	}

	private Dokumentobjekt mapDokumentobjekt(FilDetaljer filDetaljer, String tema) {
		Dokumentobjekt dokumentobjekt = new Dokumentobjekt();
		dokumentobjekt.setSystemID(mapSystemID(filDetaljer.getUuid()));
		dokumentobjekt.setVersjonsnummer(toBigInteger(1));
		dokumentobjekt.setVariantformat("Arkivformat");
		dokumentobjekt.setFormat("PDF/A");
		dokumentobjekt.setOpprettetDato(dateToXMLGregorianCalendar(filDetaljer.getDatoOpprettet()));
		dokumentobjekt.setOpprettetAv(setSystembrukerOrBeriket(filDetaljer.getOpprettetAv(), filDetaljer.getOpprettetAvBeriketNavn()));
		dokumentobjekt.setReferanseDokumentfil("DOKUMENTER/" + tema + "/" + filDetaljer.getFilUuid() + ".pdf");
		dokumentobjekt.setSjekksum(filDetaljer.getSha256hashBeriket());
		dokumentobjekt.setSjekksumAlgoritme("SHA-256");
		dokumentobjekt.setFilstoerrelse(toBigInteger(filDetaljer.getFilstorrelseBeriket()));

		return dokumentobjekt;
	}

	private String getSaksAnsvarlig(List<Journalpost> journalposter) {
		Journalpost journalpost = journalposter.stream().min(Comparator.comparing(Journalpost::getId)).orElseThrow(NoSuchElementException::new);
		return setSystembrukerOrBeriket(journalpost.getEndretAv(), journalpost.getEndretAvBeriketNavn());
	}

	private String determinePartID(Sak sak) {
		return sak.getBruker().isOrganisasjon() ? sak.getBruker().getNavn() : sak.getBruker().getId();
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

	private String mapKorrespondanseParttype(String journalpost_t) {
		return "I".equalsIgnoreCase(journalpost_t) ? "Avsender" : "Mottaker";
	}

	private boolean isSystembruker(String bruker) {
		Pattern pattern = Pattern.compile("[azAZ]\\d{8}");
		Matcher m = pattern.matcher(bruker);
		return m.matches() ? false : true;
	}

	private String setSystembrukerOrBeriket(String opprettetAv, String opprettetAvBeriketNavn) {
		return isSystembruker(opprettetAv) ? "Automatisk jobb" : opprettetAvBeriketNavn;
	}

	private int getYear(Date date){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	private SystemID mapSystemID(final UUID value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value.toString());
		return systemID;
	}

	private String getAdministrativEnhetFromTema(String tema) {
		return Tema.valueOf(tema).getAdminEnhet();
	}


}
