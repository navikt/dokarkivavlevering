package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Profile("avsluttSaker")
public class OppdaterAktoerIdService {

	private static final int BATCHSTOERRELSE = 1000;

	private final ArbeidssakRepository arbeidssakRepository;
	private final OppdaterArbeidssakForPdl OppdaterArbeidssakForPdl;

	public OppdaterAktoerIdService(ArbeidssakRepository arbeidssakRepository, OppdaterArbeidssakForPdl OppdaterArbeidssakForPdl) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.OppdaterArbeidssakForPdl = OppdaterArbeidssakForPdl;
	}

	public void oppdaterUtdaterteAktoerIder() {
		log.info("AvsluttAlleSaker starter oppdateringen av utdaterte aktoerId'er");

		List<Long> ubehandledeSakerMedAktoerId = arbeidssakRepository.hentAlleUbehandledeSakerMedAktoerId();
		if (ubehandledeSakerMedAktoerId.isEmpty()) {
			log.info("Fant ingen ubehandlede saker med aktoerId. Prøver ikke å oppdatere aktørIder fra PDL.");
			return;
		}

		List<List<Long>> sakIdsPartitioned = Lists.partition(ubehandledeSakerMedAktoerId, BATCHSTOERRELSE);
		sakIdsPartitioned.forEach(OppdaterArbeidssakForPdl::oppdaterUtdaterteAktoerIderForPartisjon);

		log.info("AvsluttAlleSaker har oppdatert alle utdaterte aktoerId'er");
	}


}
