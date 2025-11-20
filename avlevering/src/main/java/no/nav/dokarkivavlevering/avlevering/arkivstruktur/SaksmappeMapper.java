package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.MedOpprettetAv;
import no.nav.dokarkivavlevering.avlevering.domain.MedOpprettetAvNavn;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static no.nav.dokarkivavlevering.avlevering.AvleveringSakBerikerMapper.AUTOMATISK_JOBB;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.INNGAAENDE;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.UTGAAENDE;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getYear;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.isTemaAvleverMedDokumenter;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@Profile("genererAvlevering")
public class SaksmappeMapper {

	public static final String DOKUMENT_STATUS_FERDIGSTILT = "FERDIGSTILT";
	private final JournaldatoMapper journaldatoMapper;

	public SaksmappeMapper(JournaldatoMapper journaldatoMapper) {
		this.journaldatoMapper = journaldatoMapper;
	}

	public Saksmappe map(Sak sak) {
		Saksmappe mappe = new Saksmappe();
		mappe.setSystemID(AvleveringUtils.mapSystemID(sak.getUuid()));
		mappe.setMappeID(sak.getId().toString());
		mappe.setOpprettetDato(sak.getOpprettetTidspunkt());
		mappe.setOpprettetAv(resolveOpprettetAv(sak));
		mappe.setTittel(temaNavnDecode(sak.getTema()));
		mappe.getParts().add(mapPart(sak));
		mappe.setSaksaar(toBigInteger(getYear(sak.getOpprettetTidspunkt())));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setSaksdato(sak.getOpprettetTidspunkt().toLocalDate());
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
		part.setPartID(sak.getBrukerMedNavnedata().getId());
		part.setPartNavn(sak.getBrukerMedNavnedata().getFulltnavn(sak.getOpprettetTidspunkt().atZone(ZoneId.of("Europe/Oslo"))));
		return part;
	}

	private Registrering mapRegistrering(Journalpost journalpost, String tema) {
		final LocalDateTime journaldato = journaldatoMapper.mapJournaldato(journalpost);
		final Long journalpostId = journalpost.getId();
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering = new no.arkivverket.standarder.noark5.arkivstruktur.Journalpost();
		registrering.setSystemID(AvleveringUtils.mapSystemID(journalpost.getUuid()));
		registrering.setOpprettetDato(journalpost.getDatoOpprettet());
		registrering.setOpprettetAv(resolveOpprettetAvNavn(journalpost));
		registrering.setRegistreringsID(journalpostId.toString());
		registrering.setTittel(journalpost.getInnhold());
		registrering.setJournalaar(toBigInteger(getYear(journaldato)));
		registrering.setJournalsekvensnummer(toBigInteger(journalpostId));
		registrering.setJournalpostnummer(toBigInteger(journalpostId));
		registrering.setJournalposttype(determineJournalPostType(journalpost.getType()));
		registrering.setJournaldato(journaldato.toLocalDate());
		registrering.setSendtDato(determineSendtDato(journalpost, journaldato));
		registrering.setJournalstatus("Arkivert");

		//Skal kun settes hvis det ikke er et notat.
		if (!"N".equals(journalpost.getType())) {
			registrering.getKorrespondanseparts().add(mapKorrespondansePart(journalpost));
		}
		//Skal kun settes om journalpost.getDatoDokument() != null
		if (journalpost.getDatoDokument() != null) {
			registrering.setDokumentetsDato(journalpost.getDatoDokument().toLocalDate());
		}
		//Skal kun settes om journalpost.getDatoMottatt() != null && journalpostType == "I"
		if (INNGAAENDE.equals(journalpost.getType()) && journalpost.getDatoMottatt() != null) {
			registrering.setMottattDato(journalpost.getDatoMottatt());
		}
		for (DokumentInfo dokumentInfo : journalpost.getDok()) {
			registrering.getDokumentbeskrivelses().add(mapDokumentBeskrivelse(dokumentInfo, tema, journalpostId.toString()));
		}

		return registrering;
	}

	private Korrespondansepart mapKorrespondansePart(Journalpost journalpost) {
		Korrespondansepart korrespondansepart = new Korrespondansepart();
		korrespondansepart.setKorrespondanseparttype(mapKorrespondanseParttype(journalpost.getType()));
		korrespondansepart.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		korrespondansepart.setSaksbehandler(resolveOpprettetAvNavn(journalpost));
		return korrespondansepart;
	}

	private Dokumentbeskrivelse mapDokumentBeskrivelse(DokumentInfo dokumentInfo, String tema, String journalpostId) {
		Dokumentbeskrivelse dokumentbeskrivelse = new Dokumentbeskrivelse();
		dokumentbeskrivelse.setSystemID(AvleveringUtils.mapSystemID(dokumentInfo.getUuid()));
		dokumentbeskrivelse.setDokumenttype(dokumentInfo.getKategoriDecode());
		dokumentbeskrivelse.setDokumentstatus(mapDokumentstatus(dokumentInfo));
		dokumentbeskrivelse.setTittel(dokumentInfo.getTittel());
		dokumentbeskrivelse.setOpprettetDato(dokumentInfo.getDatoOpprettet());
		dokumentbeskrivelse.setOpprettetAv(resolveOpprettetAv(dokumentInfo));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(dokumentInfo.getRelTilknyttetSom());
		dokumentbeskrivelse.setDokumentnummer(toBigInteger(dokumentInfo.getId()));
		dokumentbeskrivelse.setTilknyttetDato(dokumentInfo.getRelDatoOpprettet());
		dokumentbeskrivelse.setTilknyttetAv(dokumentInfo.getRelOpprettetAvBeriketNavn());

		if (isTemaAvleverMedDokumenter(tema)) {
			for (FilDetaljer filDetaljer : dokumentInfo.getFd()) {
				dokumentbeskrivelse.getDokumentobjekts().add(mapDokumentobjekt(filDetaljer, tema, journalpostId));
			}
		}
		return dokumentbeskrivelse;
	}

	private String mapDokumentstatus(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getStatus() == null || DOKUMENT_STATUS_FERDIGSTILT.equals(dokumentInfo.getStatus())) {
			return "Dokumentet er ferdigstilt";
		} else {
			return "Dokumentet er under redigering";
		}
	}

	private Dokumentobjekt mapDokumentobjekt(FilDetaljer filDetaljer, String tema, String journalpostId) {
		Dokumentobjekt dokumentobjekt = new Dokumentobjekt();
		dokumentobjekt.setSystemID(AvleveringUtils.generateSystemID());
		dokumentobjekt.setVersjonsnummer(toBigInteger(1));
		dokumentobjekt.setVariantformat("Arkivformat");
		dokumentobjekt.setFormat("PDF/A");
		dokumentobjekt.setOpprettetDato(filDetaljer.getDatoOpprettet());
		dokumentobjekt.setOpprettetAv(resolveOpprettetAv(filDetaljer));
		dokumentobjekt.setReferanseDokumentfil("DOKUMENTER/" + tema + "/" + journalpostId + "_" + filDetaljer.getFilUuid() + ".pdf");
		dokumentobjekt.setSjekksum(filDetaljer.getSha256hashBeriket());
		dokumentobjekt.setSjekksumAlgoritme("SHA-256");
		dokumentobjekt.setFilstoerrelse(toBigInteger(filDetaljer.getFilstorrelseBeriket()));

		return dokumentobjekt;
	}

	private String getSaksAnsvarlig(List<Journalpost> journalposter) {
		Journalpost journalpost = journalposter.stream().min(Comparator.comparing(Journalpost::getId)).orElseThrow(NoSuchElementException::new);
		if (isBlank(journalpost.getEndretAv())) {
			return Bruker.UKJENT_PERSON;
		}
		return journalpost.getEndretAvBeriketNavn();
	}

	private String determineJournalPostType(String journalpostType) {
		return switch (journalpostType) {
			case UTGAAENDE -> "Utgående dokument";
			case INNGAAENDE -> "Inngående dokument";
			default -> "Organinternt dokument uten oppfølging";
		};
	}

	private LocalDateTime determineSendtDato(Journalpost journalpost, LocalDateTime journaldato) {
		if (!UTGAAENDE.equals(journalpost.getType())) {
			return null;
		}

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

	private String mapKorrespondanseParttype(String journalpostType) {
		return INNGAAENDE.equalsIgnoreCase(journalpostType) ? "Avsender" : "Mottaker";
	}

	private String getAdministrativEnhetFromTema(String tema) {
		return Tema.valueOf(tema).getAdminEnhet();
	}

	private static String resolveOpprettetAvNavn(MedOpprettetAvNavn aktoer) {
		if (aktoer.getOpprettetAvNavn() != null) {
			return aktoer.getOpprettetAvNavn();
		}
		return resolveOpprettetAv(aktoer);
	}

	private static String resolveOpprettetAv(MedOpprettetAv aktoer) {
		if (aktoer.getOpprettetAv() == null && aktoer.getOpprettetAvBeriketNavn() == null) {
			return "Ukjent";
		}
		if (erSystembruker(aktoer.getOpprettetAv())) {
			return "Automatisk jobb";
		}
		return aktoer.getOpprettetAvBeriketNavn();
	}

	private static boolean erSystembruker(String opprettetAv) {
		return "9999".equalsIgnoreCase(opprettetAv) || AUTOMATISK_JOBB.equalsIgnoreCase(opprettetAv);
	}
}
