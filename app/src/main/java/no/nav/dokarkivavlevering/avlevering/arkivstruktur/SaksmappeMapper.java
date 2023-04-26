package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapXmlGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getYear;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.isStringTemaAvleverMedDokumenter;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SaksmappeMapper {

	public static final String DOKUMENT_STATUS_FERDIGSTILT = "FERDIGSTILT";
	private final JournaldatoMapper journaldatoMapper;
	private final AvleveringProperties.ArkivConfig arkivConfig;

	public SaksmappeMapper(JournaldatoMapper journaldatoMapper, AvleveringProperties avleveringProperties) {
		this.journaldatoMapper = journaldatoMapper;
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	public Saksmappe map(Sak sak) {
		Saksmappe mappe = new Saksmappe();
		mappe.setSystemID(mapSystemID(sak.getUuid()));
		mappe.setMappeID(sak.getId().toString());
		mappe.setOpprettetDato(mapXmlGregorianCalendar(sak.getOpprettetTidspunkt()));
		mappe.setOpprettetAv(sak.getOpprettetAvBeriketNavn());
		mappe.setTittel(temaNavnDecode(sak.getTema()));
		mappe.getReferanseArkivdels().add(arkivConfig.getArkivdelConfig().getSystemID());
		mappe.getParts().add(mapPart(sak));
		mappe.setSaksaar(toBigInteger(getYear(sak.getOpprettetTidspunkt())));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setSaksdato(mapXmlGregorianCalendar(sak.getOpprettetTidspunkt()));
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
		part.setPartID(sak.getBruker().getId());
		part.setPartNavn(sak.getBrukerMedNavnedata().getFulltnavn(sak.getOpprettetTidspunkt().toInstant().atZone(ZoneId.of("Europe/Oslo"))));
		return part;
	}

	private Registrering mapRegistrering(Journalpost journalpost, String tema) {
		final Date journaldato = journaldatoMapper.mapJournaldato(journalpost);
		final Long journalpostId = journalpost.getId();
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering = new no.arkivverket.standarder.noark5.arkivstruktur.Journalpost();
		registrering.setSystemID(mapSystemID(journalpost.getUuid()));
		registrering.setOpprettetDato(mapXmlGregorianCalendar(journalpost.getDatoOpprettet()));
		registrering.setOpprettetAv(mapOpprettetAvNavn(journalpost));
		registrering.setRegistreringsID(journalpostId.toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalaar(toBigInteger(getYear(journaldato)));
		registrering.setJournalsekvensnummer(toBigInteger(journalpostId));
		registrering.setJournalpostnummer(toBigInteger(journalpostId));
		registrering.setJournalposttype(determineJournalPostType(journalpost.getType()));
		registrering.setJournaldato(mapXmlGregorianCalendar(journaldato));
		registrering.setSendtDato(mapXmlGregorianCalendar(determineSendtDato(journalpost, journaldato)));
		registrering.setRegistreringsID(journalpostId.toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalstatus("Arkivert");

		//Skal kun settes hvis det ikke er et notat.
		if (!"N".equals(journalpost.getType())) {
			registrering.getKorrespondanseparts().add(mapKorrespondansePart(journalpost));
		}
		//Skal kun settes om journalpost.getDatoDokument() != null
		if (journalpost.getDatoDokument() != null) {
			registrering.setDokumentetsDato(mapXmlGregorianCalendar(journalpost.getDatoDokument()));
		}
		//Skal kun settes om journalpost.getDatoMottatt() != null && journalpostType == "I"
		if ("I".equals(journalpost.getType()) && journalpost.getDatoMottatt() != null) {
			registrering.setMottattDato(mapXmlGregorianCalendar(journalpost.getDatoMottatt()));
		}
		for (DokumentInfo dokumentInfo : journalpost.getDok()) {
			registrering.getDokumentbeskrivelses().add(mapDokumentBeskrivelse(dokumentInfo, tema, journalpostId.toString()));
		}

		return registrering;
	}

	private String mapOpprettetAvNavn(Journalpost journalpost) {
		if(isBlank(journalpost.getOpprettetAvNavn())) {
			if(isBlank(journalpost.getOpprettetAv())) {
				return Bruker.UKJENT_PERSON;
			}
			return journalpost.getOpprettetAvBeriketNavn();
		}
		return journalpost.getOpprettetAvNavn();
	}


	private Korrespondansepart mapKorrespondansePart(Journalpost journalpost) {
		Korrespondansepart korrespondansepart = new Korrespondansepart();
		korrespondansepart.setKorrespondanseparttype(mapKorrespondanseParttype(journalpost.getType()));
		korrespondansepart.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		korrespondansepart.setSaksbehandler(mapOpprettetAvNavn(journalpost));
		return korrespondansepart;
	}

	private Dokumentbeskrivelse mapDokumentBeskrivelse(DokumentInfo dokumentInfo, String tema, String journalpostId) {
		Dokumentbeskrivelse dokumentbeskrivelse = new Dokumentbeskrivelse();
		dokumentbeskrivelse.setSystemID(mapSystemID(dokumentInfo.getUuid()));
		dokumentbeskrivelse.setDokumenttype(dokumentInfo.getKategoriDecode());
		dokumentbeskrivelse.setDokumentstatus(mapDokumentstatus(dokumentInfo));
		dokumentbeskrivelse.setTittel(dokumentInfo.getTittel());
		dokumentbeskrivelse.setOpprettetDato(mapXmlGregorianCalendar(dokumentInfo.getDatoOpprettet()));
		dokumentbeskrivelse.setOpprettetAv(dokumentInfo.getOpprettetAvBeriketNavn());
		dokumentbeskrivelse.setTilknyttetRegistreringSom(dokumentInfo.getRelTilknyttetSom());
		dokumentbeskrivelse.setDokumentnummer(toBigInteger(dokumentInfo.getId()));
		dokumentbeskrivelse.setTilknyttetDato(mapXmlGregorianCalendar(dokumentInfo.getRelDatoOpprettet()));
		dokumentbeskrivelse.setTilknyttetAv(dokumentInfo.getRelOpprettetAvBeriketNavn());

		if(isStringTemaAvleverMedDokumenter(tema)) {
			for (FilDetaljer filDetaljer : dokumentInfo.getFd()) {
				dokumentbeskrivelse.getDokumentobjekts().add(mapDokumentobjekt(filDetaljer, tema, journalpostId));
			}
		}
		return dokumentbeskrivelse;
	}

	private String mapDokumentstatus(DokumentInfo dokumentInfo) {
		if(dokumentInfo.getStatus() == null || DOKUMENT_STATUS_FERDIGSTILT.equals(dokumentInfo.getStatus())) {
			return "Dokumentet er ferdigstilt";
		} else {
			return "Dokumentet er under redigering";
		}
	}

	private Dokumentobjekt mapDokumentobjekt(FilDetaljer filDetaljer, String tema, String journalpostId) {
		Dokumentobjekt dokumentobjekt = new Dokumentobjekt();
		dokumentobjekt.setSystemID(mapSystemID(filDetaljer.getUuid()));
		dokumentobjekt.setVersjonsnummer(toBigInteger(1));
		dokumentobjekt.setVariantformat("Arkivformat");
		dokumentobjekt.setFormat("PDF/A");
		dokumentobjekt.setOpprettetDato(mapXmlGregorianCalendar(filDetaljer.getDatoOpprettet()));
		dokumentobjekt.setOpprettetAv(filDetaljer.getOpprettetAvBeriketNavn());
		dokumentobjekt.setReferanseDokumentfil("DOKUMENTER/" + tema + "/" + journalpostId + "_" + filDetaljer.getFilUuid() + ".pdf");
		dokumentobjekt.setSjekksum(filDetaljer.getSha256hashBeriket());
		dokumentobjekt.setSjekksumAlgoritme("SHA-256");
		dokumentobjekt.setFilstoerrelse(toBigInteger(filDetaljer.getFilstorrelseBeriket()));

		return dokumentobjekt;
	}

	private String getSaksAnsvarlig(List<Journalpost> journalposter) {
		Journalpost journalpost = journalposter.stream().min(Comparator.comparing(Journalpost::getId)).orElseThrow(NoSuchElementException::new);
		if(isBlank(journalpost.getEndretAv())) {
			return Bruker.UKJENT_PERSON;
		}
		return journalpost.getEndretAvBeriketNavn();
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

	private Date determineSendtDato(Journalpost journalpost, Date journaldato) {
		switch (journalpost.getStatus()) {
			case "E":
				if (journalpost.getDatoEkspedert() != null)
					return journalpost.getDatoEkspedert();
			case "FS":
				if (journalpost.getDatoSendtPrint() != null)
					return journalpost.getDatoSendtPrint();
			default:
				return journaldato;
		}
	}

	private String mapKorrespondanseParttype(String journalpost_t) {
		return "I".equalsIgnoreCase(journalpost_t) ? "Avsender" : "Mottaker";
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
