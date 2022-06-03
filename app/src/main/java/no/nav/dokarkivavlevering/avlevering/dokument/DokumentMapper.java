package no.nav.dokarkivavlevering.avlevering.dokument;

import no.nav.dokarkivavlevering.avlevering.aspose.AsposeService;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkivavlevering.avlevering.aspose.AsposeService.convertToPDFA;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentMapper {
	public List<Dokument> map(final List<Sak> saker) {
		return saker.stream().flatMap(sak -> sak.getJp().stream()
				.flatMap(journalpost -> journalpost.getDok().stream()
						.flatMap(dokumentInfo -> dokumentInfo.getFd().stream()
								.map(fd -> new Dokument(journalpost.getId().toString(), fd.getFilUuid(), convertToPDFA(fd.getFil(), ""+dokumentInfo.getId()))))))
				.collect(Collectors.toList());
	}
}
