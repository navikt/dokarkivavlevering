package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Arkivskaper;
import no.arkivverket.standarder.noark5.loependejournal.Journalhode;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.springframework.stereotype.Component;

import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
public class LoependejournalMapper {

	private final AvleveringProperties.Periode periode;

	public LoependejournalMapper(AvleveringProperties avleveringProperties) {
		this.periode = avleveringProperties.getPeriode();
	}

	public LoependeJournal map() {
		LoependeJournal journal = new LoependeJournal();
		journal.setJournalhode(mapJournalHode());
		journal.getJournalregistrerings();
		return journal;
	}

	private Journalhode mapJournalHode() {
		Journalhode journalHode = new Journalhode();
		journalHode.setJournalStartDato(periode.getStartdato());
		journalHode.setJournalSluttDato(periode.getSluttdato());
		journalHode.setSeleksjon("journaldato");
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
