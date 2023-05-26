package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvleveringArkivstrukturKlasseService {
	private final SaksmappeMapper saksmappeMapper;
	private final KlasseMapper klasseMapper;

	public AvleveringArkivstrukturKlasseService(SaksmappeMapper saksmappeMapper, KlasseMapper klasseMapper) {
		this.saksmappeMapper = saksmappeMapper;
		this.klasseMapper = klasseMapper;
	}

	@Handler
	public Klasse avlevering(@Body final List<Sak> sakerPaginertPerTemaOgPage) {
		return klasseMapper.map(
				sakerPaginertPerTemaOgPage.get(0).getFagomrade(),
				sakerPaginertPerTemaOgPage.stream()
						.map(saksmappeMapper::map)
						.toList());
	}
}
