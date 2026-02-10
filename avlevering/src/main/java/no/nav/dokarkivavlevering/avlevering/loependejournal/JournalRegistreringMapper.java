package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.Klasse;
import no.arkivverket.standarder.noark5.loependejournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.loependejournal.Saksmappe;
import no.arkivverket.standarder.noark5.loependejournal.SystemID;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getHoveddokumentTittel;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getYear;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.isNav;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapKorrespondansepartType;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
@Profile("genererAvlevering")
public class JournalRegistreringMapper {

	private final JournaldatoMapper journaldatoMapper;

	public JournalRegistreringMapper(JournaldatoMapper journaldatoMapper) {
		this.journaldatoMapper = journaldatoMapper;
	}

	public Journalregistrering map(Sak sak, Journalpost fraJournalpost) {
		Journalregistrering registrering = new Journalregistrering();
		registrering.setJournalpost(mapJournalPost(fraJournalpost));
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
		mappe.setTittel(temaNavnDecode(sak.getTema()));

		return mappe;
	}

	private no.arkivverket.standarder.noark5.loependejournal.Journalpost mapJournalPost(Journalpost fraJournalpost) {
		final LocalDateTime journaldato = journaldatoMapper.mapJournaldato(fraJournalpost);
		no.arkivverket.standarder.noark5.loependejournal.Journalpost tilJournalpost = new no.arkivverket.standarder.noark5.loependejournal.Journalpost();
		tilJournalpost.setSystemID(mapSystemID(fraJournalpost.getUuid()));
		tilJournalpost.setJournalaar(toBigInteger(getYear(journaldato)));
		tilJournalpost.setJournalsekvensnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setJournalpostnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setTittel(getHoveddokumentTittel(fraJournalpost));
		tilJournalpost.setJournaldato(journaldato.toLocalDate());
		tilJournalpost.getKorrespondanseparts().add(mapKorrespondansepart(fraJournalpost));

		if (fraJournalpost.getDatoDokument() != null) {
			tilJournalpost.setDokumentetsDato(fraJournalpost.getDatoDokument().toLocalDate());
		}
		return tilJournalpost;
	}

	private Korrespondansepart mapKorrespondansepart(Journalpost journalpost) {
		Korrespondansepart part = new Korrespondansepart();
		part.setKorrespondanseparttype(mapKorrespondansepartType(journalpost.getType()));
		part.setKorrespondansepartNavn(mapKorrespondansepartNavn(journalpost));
		return part;
	}

	private SystemID mapSystemID(final UUID value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value.toString());
		return systemID;
	}

	public static String mapKorrespondansepartNavn(no.nav.dokarkivavlevering.avlevering.domain.Journalpost journalpost) {
		return isNav(journalpost.getType()) ? journalpost.getAvsenderMottaker() : "NAV";
	}
}
