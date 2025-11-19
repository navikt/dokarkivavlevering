package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.Klasse;
import no.arkivverket.standarder.noark5.offentligjournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.offentligjournal.Saksmappe;
import no.arkivverket.standarder.noark5.offentligjournal.SystemID;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.INNGAAENDE;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.UTGAAENDE;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getYear;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapKorrespondansepartType;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@Profile("genererAvlevering")
public class OffentligJournalRegistreringMapper {

	private static final String TEMA_PER = "PER";
	private static final String UTL_ORG ="UTL_ORG";
	private static final String HPRNR ="HPRNR";
	private static final String NOTAT ="N";

	private final JournaldatoMapper journaldatoMapper;

	public OffentligJournalRegistreringMapper(JournaldatoMapper journaldatoMapper) {
		this.journaldatoMapper = journaldatoMapper;
	}

	public Journalregistrering map(Sak sak, Journalpost fraJournalpost) {
		Journalregistrering registrering = new Journalregistrering();
		registrering.setJournalpost(mapJournalPost(fraJournalpost, sak.getTema()));
		registrering.setKlasse(mapKlasse(sak.getTema()));
		registrering.setSaksmappe(mapSaksmappe(sak));
		return registrering;
	}

	private Klasse mapKlasse(String tema) {
		Klasse klasse = new Klasse();
		klasse.setKlasseID(tema);
		klasse.setTittel(temaNavnDecode(tema));
		return klasse;
	}

	private Saksmappe mapSaksmappe(Sak sak) {
		Saksmappe mappe = new Saksmappe();
		mappe.setSaksaar(toBigInteger(getYear(sak.getOpprettetTidspunkt())));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setOffentligTittel(temaNavnDecode(sak.getTema()));

		return mappe;
	}

	private no.arkivverket.standarder.noark5.offentligjournal.Journalpost mapJournalPost(Journalpost fraJournalpost, String tema) {
		final LocalDateTime journaldato = journaldatoMapper.mapJournaldato(fraJournalpost);
		no.arkivverket.standarder.noark5.offentligjournal.Journalpost tilJournalpost = new no.arkivverket.standarder.noark5.offentligjournal.Journalpost();
		tilJournalpost.setSystemID(mapSystemID(fraJournalpost.getUuid()));
		tilJournalpost.setJournalaar(toBigInteger(getYear(journaldato)));
		tilJournalpost.setJournalsekvensnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setJournalpostnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setOffentligTittel(fraJournalpost.getInnhold());
		tilJournalpost.setJournaldato(journaldato.toLocalDate());
		tilJournalpost.getKorrespondanseparts().add(mapKorrespondansepart(fraJournalpost, tema));
		tilJournalpost.setSkjermingMetadata(mapSkjermingMetadata(fraJournalpost, tema));
		tilJournalpost.setSkjermingshjemmel(mapSkjermingHjemmel(fraJournalpost, tema));
		if (fraJournalpost.getDatoDokument() != null) {
			tilJournalpost.setDokumentetsDato(fraJournalpost.getDatoDokument().toLocalDate());
		}
		return tilJournalpost;
	}

	private Korrespondansepart mapKorrespondansepart(Journalpost journalpost, String sakTema) {
		if(NOTAT.equalsIgnoreCase(journalpost.getType())){
			return null;
		}
		Korrespondansepart part = new Korrespondansepart();
		part.setKorrespondanseparttype(mapKorrespondansepartType(journalpost.getType()));

		if(TEMA_PER.equals(sakTema)){
			part.setKorrespondansepartNavn("****");
		}
		else if(!isBlank(journalpost.getOffentligJournalAvsenderMottaker())){
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		}
		else if (!isBlank(journalpost.getAvsenderMottakerId()) && isIdLength_3_8_9_13(journalpost.getAvsenderMottakerId())){
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		}
		else if (HPRNR.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType()) ||
				UTL_ORG.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType())) {
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		}
		else{
			part.setKorrespondansepartNavn("****");
		}
		return part;
	}

	private SystemID mapSystemID(final UUID value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value.toString());
		return systemID;
	}

	private String mapSkjermingMetadata(Journalpost journalpost, String sakTema){
		String skjermingstekst ="";
		if(INNGAAENDE.equalsIgnoreCase(journalpost.getType())) {
			skjermingstekst = "Skjerming navn avsender";
		}
		else if(UTGAAENDE.equalsIgnoreCase(journalpost.getType())) {
			skjermingstekst = "Skjerming navn mottaker";
		}
		else {
			return null;
		}
		return determineSkjerming(skjermingstekst, journalpost, sakTema);
	}

	private String mapSkjermingHjemmel(Journalpost journalpost, String sakTema) {
		if(NOTAT.equalsIgnoreCase(journalpost.getType())) {
			return null;
		}
		return determineSkjerming("Offl. § 13 1. ledd, jf fvl § 13 1. ledd nr. 2 / NAV-loven § 7", journalpost, sakTema);
	}

	private String determineSkjerming(String begrunnelse, Journalpost journalpost, String sakTema) {
		if (TEMA_PER.equals(sakTema)) {
			return begrunnelse;
		} else if (!isBlank(journalpost.getOffentligJournalAvsenderMottaker())) {
			return null;
		} else if (!isBlank(journalpost.getAvsenderMottakerId()) && isIdLength_3_8_9_13(journalpost.getAvsenderMottakerId())) {
			return null;
		} else if (HPRNR.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType()) ||
				UTL_ORG.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType())) {
			return null;
		}
		return begrunnelse;
	}

	private boolean isIdLength_3_8_9_13(String avsenderMottakerId) {
		return switch (avsenderMottakerId.length()) {
			case 3, 8, 9, 13 -> true;
			default -> false;
		};
	}
}
