package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvleveringArkivstrukturService {
	private final SaksmappeMapper saksmappeMapper;
	private final KlasseMapper klasseMapper;
	private final ArkivdelMapper arkivdelMapper;
	private final ArkivMapper arkivMapper;

	public AvleveringArkivstrukturService(SaksmappeMapper saksmappeMapper, KlasseMapper klasseMapper, ArkivdelMapper arkivdelMapper, ArkivMapper arkivMapper) {
		this.saksmappeMapper = saksmappeMapper;
		this.klasseMapper = klasseMapper;
		this.arkivdelMapper = arkivdelMapper;
		this.arkivMapper = arkivMapper;
	}

	@Handler
	public Arkiv avlevering(@Body final List<Sak> saker) {
		return arkivMapper.map(
				saker.stream()
				.collect(Collectors.groupingBy(sak -> sak.getFagomrade().getFagomrade()))
				.values()
				.stream()
				.map(sakerPerTema -> {
					Fagomrade fagomradeForPartition = sakerPerTema.get(0).getFagomrade();
					return arkivdelMapper.map(fagomradeForPartition,
							klasseMapper.map(fagomradeForPartition,
									sakerPerTema.stream().map(saksmappeMapper::map).toList()));
				}).toList());
	}
}
