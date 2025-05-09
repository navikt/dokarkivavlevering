package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.ENDELIGE_STATUSER;

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

		List<Long> alleSaksIder = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen(ENDELIGE_STATUSER);
		List<List<Long>> sakIdsPartitioned = Lists.partition(alleSaksIder, BATCHSTOERRELSE);

		sakIdsPartitioned.forEach(OppdaterArbeidssakForPdl::oppdaterUtdaterteAktoerIderForPartisjon);
		log.info("AvsluttAlleSaker har oppdatert alle utdaterte aktoerId'er");
	}


}
