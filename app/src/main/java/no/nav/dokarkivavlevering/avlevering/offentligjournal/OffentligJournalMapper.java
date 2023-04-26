package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Arkivskaper;
import no.arkivverket.standarder.noark5.offentligjournal.Journalhode;
import no.arkivverket.standarder.noark5.offentligjournal.OffentligJournal;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.springframework.stereotype.Component;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapXmlGregorianCalendar;
import static org.apache.camel.converter.ObjectConverter.toBigInteger;

@Component
public class OffentligJournalMapper {

	private final AvleveringProperties.Periode periode;

	public OffentligJournalMapper(AvleveringProperties avleveringProperties) {
		this.periode = avleveringProperties.getPeriode();
	}

	public OffentligJournal map() {
		OffentligJournal journal = new OffentligJournal();
		journal.setJournalhode(mapJournalHode());
		journal.getJournalregistrerings();
		return journal;
	}

	private Journalhode mapJournalHode() {
		Journalhode journalHode = new Journalhode();
		journalHode.setJournalStartDato(mapXmlGregorianCalendar(periode.getStartdato()));
		journalHode.setJournalSluttDato(mapXmlGregorianCalendar(periode.getSluttdato()));
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
