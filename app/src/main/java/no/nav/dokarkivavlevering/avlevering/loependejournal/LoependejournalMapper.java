package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Arkivskaper;
import no.arkivverket.standarder.noark5.loependejournal.Journalhode;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
import org.springframework.stereotype.Component;

import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
public class LoependejournalMapper {
	public LoependeJournal map() {
		LoependeJournal journal = new LoependeJournal();
		journal.setJournalhode(mapJournalHode());
		journal.getJournalregistrerings();
		return journal;
	}

	private Journalhode mapJournalHode() {
		Journalhode journalHode = new Journalhode();
		//TODO: input.periodeStart
		//journalHode.setJournalStartDato(dateToXMLGregorianCalendar());
		//TODO: input.periodeSlutt
		//journalHode.setJournalSluttDato(dateToXMLGregorianCalendar("${value}".. eller noe));
		journalHode.setSeleksjon("journaldato");
		//TODO: "antall journalregistrering i filen". Menes det her i arkivstruktur.xml eller i saken?
		journalHode.setAntallJournalposter(toBigInteger("-1"));
		journalHode.getArkivskapers().add(mapArkivSkaper());
		return journalHode;
	}

	private Arkivskaper mapArkivSkaper() {
		Arkivskaper arkivskaper = new Arkivskaper();
		arkivskaper.setArkivskaperID("889 640 782");
		arkivskaper.setArkivskaperNavn("Arbeids- og velferdsetaten");
		return arkivskaper;
	}

}
