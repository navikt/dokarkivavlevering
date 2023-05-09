package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvleveringArkivstrukturService {
	private final SaksmappeMapper saksmappeMapper;
	private final ArkivdelMapper arkivdelMapper;

	public AvleveringArkivstrukturService(SaksmappeMapper saksmappeMapper, ArkivdelMapper arkivdelMapper) {
		this.saksmappeMapper = saksmappeMapper;
		this.arkivdelMapper = arkivdelMapper;
	}

	@Handler
	public List<Arkivdel> avlevering(@Body final List<Sak> saker) {
		return saker.stream()
				.collect(Collectors.groupingBy(sak -> sak.getFagomrade().getFagomrade()))
				.values()
				.stream()
				.map(sakerPerTema -> arkivdelMapper.map(sakerPerTema.get(0).getFagomrade(),
						sakerPerTema.stream().map(saksmappeMapper::map).toList())
				).toList();
	}
}
