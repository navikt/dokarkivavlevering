package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LoependeJournalregistreringService {

	private final JournalRegistreringMapper journalRegistreringMapper;

	public LoependeJournalregistreringService(JournalRegistreringMapper journalRegistreringMapper) {

		this.journalRegistreringMapper = journalRegistreringMapper;
	}

	@Handler
	public List<Journalregistrering> avlevering(@Body final List<Sak> saker) {
		return saker.stream().flatMap(sak ->
				sak.getJournalposter().stream().map(journalpost ->
						journalRegistreringMapper.map(sak, journalpost))).collect(Collectors.toList());
	}
}
