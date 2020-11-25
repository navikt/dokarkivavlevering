package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringArkivstrukturService {
	private final SaksmappeMapper saksmappeMapper;

	public AvleveringArkivstrukturService(SaksmappeMapper saksmappeMapper) {
		this.saksmappeMapper = saksmappeMapper;
	}

	@Handler
	public List<Saksmappe> avlevering(@Body final List<Sak> saker) {
		return saker.stream()
				.map(saksmappeMapper::map)
				.collect(Collectors.toList());
	}
}
