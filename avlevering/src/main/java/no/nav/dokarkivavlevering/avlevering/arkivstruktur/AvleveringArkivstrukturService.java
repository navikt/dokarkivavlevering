package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Profile("genererAvlevering")
public class AvleveringArkivstrukturService {
	private final KlasseMapper klasseMapper;
	private final ArkivdelMapper arkivdelMapper;
	private final ArkivMapper arkivMapper;

	public AvleveringArkivstrukturService(KlasseMapper klasseMapper, ArkivdelMapper arkivdelMapper, ArkivMapper arkivMapper) {
		this.klasseMapper = klasseMapper;
		this.arkivdelMapper = arkivdelMapper;
		this.arkivMapper = arkivMapper;
	}

	@Handler
	public Arkiv avlevering(@Body Fagomrade fagomrade) {
		return arkivMapper.map(
				List.of(
						arkivdelMapper.map(fagomrade,
								klasseMapper.map(fagomrade, Collections.emptyList())
						)));
	}
}
