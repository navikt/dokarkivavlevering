package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.Klasse;
import no.arkivverket.standarder.noark5.offentligjournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.offentligjournal.Saksmappe;
import no.arkivverket.standarder.noark5.offentligjournal.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.dateToXMLGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.isNav;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapKorrespondansepartNavn;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapKorrespondansepartType;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
public class OffentligJournalRegistreringMapper {

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
		mappe.setOffentligTittel(temaNavnDecode(sak.getTema()));

		return mappe;
	}

	private no.arkivverket.standarder.noark5.offentligjournal.Journalpost mapJournalPost(Journalpost fraJournalpost) {
		no.arkivverket.standarder.noark5.offentligjournal.Journalpost tilJournalpost = new no.arkivverket.standarder.noark5.offentligjournal.Journalpost();
		tilJournalpost.setSystemID(mapSystemID(fraJournalpost.getUuid()));
		tilJournalpost.setJournalaar(toBigInteger(getYear(fraJournalpost.getDatoOpprettet())));
		tilJournalpost.setJournalsekvensnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setJournalpostnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setOffentligTittel(fraJournalpost.getInnhold());
		tilJournalpost.setJournaldato(dateToXMLGregorianCalendar(fraJournalpost.getDatoJournal()));
		tilJournalpost.getKorrespondanseparts().add(mapKorrespondansepart(fraJournalpost));
		tilJournalpost.setSkjermingshjemmel("Offentleglova § 13");

		if (isNav(fraJournalpost.getType())) {
			if (fraJournalpost.getType().equalsIgnoreCase("I")) {
				tilJournalpost.setSkjermingMetadata("Skjerming navn avsender");
			} else {
				tilJournalpost.setSkjermingMetadata("Skjerming navn mottaker");
			}
		}

		if (fraJournalpost.getDatoDokument() != null) {
			tilJournalpost.setDokumentetsDato(dateToXMLGregorianCalendar(fraJournalpost.getDatoDokument()));
		}
		return tilJournalpost;
	}

	private int getYear(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	private Korrespondansepart mapKorrespondansepart(Journalpost journalpost) {
		Korrespondansepart part = new Korrespondansepart();
		part.setKorrespondanseparttype(mapKorrespondansepartType(journalpost.getType()));
		if (isNav(journalpost.getType())) {
			part.setKorrespondansepartNavn("****");
		} else {
			part.setKorrespondansepartNavn(mapKorrespondansepartNavn(journalpost));
		}
		return part;
	}


	private SystemID mapSystemID(final UUID value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value.toString());
		return systemID;
	}
}
