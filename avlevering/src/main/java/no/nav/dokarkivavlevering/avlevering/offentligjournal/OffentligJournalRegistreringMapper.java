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

import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.I;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.N;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.U;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getYear;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapKorrespondansepartType;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Profile("genererAvlevering")
public class OffentligJournalRegistreringMapper {

	private static final String TEMA_PER = "PER";
	private static final String UTL_ORG = "UTL_ORG";
	private static final String HPRNR = "HPRNR";

	private final JournaldatoMapper journaldatoMapper;

	public OffentligJournalRegistreringMapper(JournaldatoMapper journaldatoMapper) {
		this.journaldatoMapper = journaldatoMapper;
	}

	public Journalregistrering map(Sak sak, Journalpost fraJournalpost) {
		Journalregistrering registrering = new Journalregistrering();
		registrering.setJournalpost(mapJournalpost(fraJournalpost, sak.getTema()));
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

	private no.arkivverket.standarder.noark5.offentligjournal.Journalpost mapJournalpost(Journalpost fraJournalpost, String tema) {
		final LocalDateTime journaldato = journaldatoMapper.mapJournaldato(fraJournalpost);
		no.arkivverket.standarder.noark5.offentligjournal.Journalpost tilJournalpost = new no.arkivverket.standarder.noark5.offentligjournal.Journalpost();
		tilJournalpost.setSystemID(mapSystemID(fraJournalpost.getUuid()));
		tilJournalpost.setJournalaar(toBigInteger(getYear(journaldato)));
		tilJournalpost.setJournalsekvensnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setJournalpostnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setOffentligTittel(fraJournalpost.getHoveddokumentTittel());
		tilJournalpost.setJournaldato(journaldato.toLocalDate());
		tilJournalpost.getKorrespondanseparts().add(mapKorrespondansepart(fraJournalpost, tema));

		if (!N.name().equalsIgnoreCase(fraJournalpost.getType())) {
			tilJournalpost.setSkjermingMetadata(mapSkjermingMetadata(fraJournalpost, tema));
			tilJournalpost.setSkjermingshjemmel(mapSkjermingshjemmel(fraJournalpost, tema));
		}
		if (fraJournalpost.getDatoDokument() != null) {
			tilJournalpost.setDokumentetsDato(fraJournalpost.getDatoDokument().toLocalDate());
		}
		return tilJournalpost;
	}

	private Korrespondansepart mapKorrespondansepart(Journalpost journalpost, String sakTema) {
		Korrespondansepart part = new Korrespondansepart();
		part.setKorrespondanseparttype(mapKorrespondansepartType(journalpost.getType()));

		if (TEMA_PER.equals(sakTema)) {
			part.setKorrespondansepartNavn("****");
		} else if (!isBlank(journalpost.getOffentligJournalAvsenderMottaker())) {
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		} else if (!isBlank(journalpost.getAvsenderMottakerId()) && isIdLength_3_8_9_13(journalpost.getAvsenderMottakerId())) {
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		} else if (HPRNR.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType()) ||
				UTL_ORG.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType())) {
			part.setKorrespondansepartNavn(journalpost.getAvsenderMottaker());
		} else {
			part.setKorrespondansepartNavn("****");
		}
		return part;
	}

	private SystemID mapSystemID(final UUID value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value.toString());
		return systemID;
	}

	private String mapSkjermingMetadata(Journalpost journalpost, String sakTema) {
		if (I.name().equalsIgnoreCase(journalpost.getType())) {
			if (TEMA_PER.equals(sakTema)) {
				return "Skjerming navn avsender";
			}
			return skalSkjermes(journalpost) ? "Skjerming navn avsender" : null;

		} else if (U.name().equalsIgnoreCase(journalpost.getType())) {
			if (TEMA_PER.equals(sakTema)) {
				return "Skjerming navn mottaker";
			}
			return skalSkjermes(journalpost) ? "Skjerming navn mottaker" : null;
		} else {
			return null;
		}
	}

	private String mapSkjermingshjemmel(Journalpost journalpost, String sakTema) {
		if (TEMA_PER.equals(sakTema)) {
			return "Offl. § 13 1. ledd, jf fvl § 13 1. ledd nr. 2 / NAV-loven § 7";
		}
		return skalSkjermes(journalpost) ? "Offl. § 13 1. ledd, jf fvl § 13 1. ledd nr. 1 / NAV-loven § 7" : null;
	}

	private boolean skalSkjermes(Journalpost journalpost) {
		if (isNotBlank(journalpost.getOffentligJournalAvsenderMottaker())) {
			return false;
		} else if (isNotBlank(journalpost.getAvsenderMottakerId()) && isIdLength_3_8_9_13(journalpost.getAvsenderMottakerId())) {
			return false;
		} else if (HPRNR.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType()) ||
				UTL_ORG.equalsIgnoreCase(journalpost.getAvsenderMottakerIdType())) {
			return false;
		}
		return true;
	}

	private boolean isIdLength_3_8_9_13(String avsenderMottakerId) {
		return switch (avsenderMottakerId.length()) {
			case 3, 8, 9, 13 -> true;
			default -> false;
		};
	}
}
