package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("genererAvlevering")
public class JournalRegistreringService {
	private final OffentligJournalRegistreringMapper journalRegistreringMapper;

	public JournalRegistreringService(OffentligJournalRegistreringMapper journalRegistreringMapper) {

		this.journalRegistreringMapper = journalRegistreringMapper;
	}

	@Handler
	public List<Journalregistrering> avlevering(@Body final List<Sak> saker) {
		return saker.stream().flatMap(sak ->
				sak.getJp().stream().map(journalpost ->
						journalRegistreringMapper.map(sak, journalpost))).collect(Collectors.toList());
	}
}
