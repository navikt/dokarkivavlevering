package no.nav.dokarkivavlevering.avlevering;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.SaksmappeMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringSakService {
	private final AvleveringSakBerikerService avleveringSakBerikerService;
	private final SaksmappeMapper saksmappeMapper;

	public AvleveringSakService(AvleveringSakBerikerService avleveringSakBerikerService, SaksmappeMapper saksmappeMapper) {
		this.avleveringSakBerikerService = avleveringSakBerikerService;
		this.saksmappeMapper = saksmappeMapper;
	}

	@Handler
	public List<Saksmappe> avlevering(final List<Sak> saker, @ExchangeProperty(AvleveringRoute.PROPERTY_TEMA) final String tema) {
		return avleveringSakBerikerService.berikSaker(saker, tema)
				.stream().map(saksmappeMapper::map)
				.collect(Collectors.toList());
	}
}
