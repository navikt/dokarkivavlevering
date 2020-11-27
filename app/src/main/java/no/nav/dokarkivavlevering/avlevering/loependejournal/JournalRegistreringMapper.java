package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.Klasse;
import no.arkivverket.standarder.noark5.loependejournal.Korrespondansepart;
import no.arkivverket.standarder.noark5.loependejournal.Saksmappe;
import no.arkivverket.standarder.noark5.loependejournal.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.dateToXMLGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
public class JournalRegistreringMapper {


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

	private Saksmappe mapSaksmappe(Sak sak){
		Saksmappe mappe = new Saksmappe();
		mappe.setSaksaar(toBigInteger(sak.getOpprettetTidspunkt()));
		mappe.setSakssekvensnummer(toBigInteger(sak.getId()));
		mappe.setTittel(temaNavnDecode(sak.getTema()));

		return mappe;
	}

	private no.arkivverket.standarder.noark5.loependejournal.Journalpost mapJournalPost(Journalpost fraJournalpost) {
		no.arkivverket.standarder.noark5.loependejournal.Journalpost tilJournalpost = new no.arkivverket.standarder.noark5.loependejournal.Journalpost();
		tilJournalpost.setSystemID(mapSystemID(fraJournalpost.getUuid()));
		tilJournalpost.setJournalaar(toBigInteger(getYear(fraJournalpost.getDatoOpprettet())));
		tilJournalpost.setJournalsekvensnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setJournalpostnummer(toBigInteger(fraJournalpost.getId()));
		tilJournalpost.setTittel(fraJournalpost.getInnhold());
		tilJournalpost.setJournaldato(dateToXMLGregorianCalendar(fraJournalpost.getDatoJournal()));
		tilJournalpost.getKorrespondanseparts().add(mapKorrespondansepart(fraJournalpost));

		if(fraJournalpost.getDatoDokument() != null) {
			tilJournalpost.setDokumentetsDato(dateToXMLGregorianCalendar(fraJournalpost.getDatoDokument()));
		}
		return tilJournalpost;
	}

	private int getYear(Date date){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
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

	private String mapKorrespondansepartNavn(no.nav.dokarkivavlevering.avlevering.domain.Journalpost journalpost){
		return isNav(journalpost.getType()) ? journalpost.getAvsenderMottaker() : "NAV";
	}

	private boolean isNav(String journalpostType) {
		return "I".equalsIgnoreCase(journalpostType) |"U".equalsIgnoreCase(journalpostType) ?
				true : false;
	}

	private String mapKorrespondansepartType(String journalpostType) {
		return "I".equalsIgnoreCase(journalpostType) ? "Avsender" :
				"U".equalsIgnoreCase(journalpostType) ? "Mottaker" : "Intern avsender";
	}
}
