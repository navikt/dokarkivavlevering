package no.nav.dokarkivavlevering.avlevering.dokument;

import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentMapper {
	public List<FilDetaljer> map(final List<Sak> saker) {
		return saker.stream().flatMap(sak -> sak.getJournalposter().stream()
				.flatMap(journalpost -> journalpost.getDokumenter().stream()
						.flatMap(dokumentInfo -> dokumentInfo.getFildetaljer().stream())))
				.collect(Collectors.toList());
	}
}
