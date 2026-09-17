package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Arkivskaper;
import no.arkivverket.standarder.noark5.loependejournal.Journalhode;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Profile("genererAvlevering")
public class LoependejournalMapper {

	public static LoependeJournal map(LocalDate startdato, LocalDate sluttdato) {
		LoependeJournal journal = new LoependeJournal();
		journal.setJournalhode(mapJournalHode(startdato, sluttdato));
		journal.getJournalregistrerings();
		return journal;
	}

	private static Journalhode mapJournalHode(LocalDate startdato, LocalDate sluttdato) {
		Journalhode journalHode = new Journalhode();
		journalHode.setJournalStartDato(startdato);
		journalHode.setJournalSluttDato(sluttdato);
		journalHode.setSeleksjon("journaldato");
		journalHode.setAntallJournalposter(toBigInteger("-1"));
		journalHode.getArkivskapers().add(mapArkivSkaper());
		return journalHode;
	}

	private static Arkivskaper mapArkivSkaper() {
		Arkivskaper arkivskaper = new Arkivskaper();
		arkivskaper.setArkivskaperID("889 640 782");
		arkivskaper.setArkivskaperNavn("Arbeids- og velferdsetaten");
		return arkivskaper;
	}
}