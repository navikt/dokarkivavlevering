package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.ENDELIGE_STATUSER;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@Profile("avsluttSaker")
public class OppdaterAktoerIdService {

	private static final int BATCHSTOERRELSE = 100;

	private final ArbeidssakRepository arbeidssakRepository;
	private final doUpdateArbeidssakForPdl doUpdateArbeidssakForPdl;
	private final AvsluttSakProperties avsluttSakProperties;

	public OppdaterAktoerIdService(ArbeidssakRepository arbeidssakRepository, doUpdateArbeidssakForPdl doUpdateArbeidssakForPdl, AvsluttSakProperties avsluttSakProperties) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.doUpdateArbeidssakForPdl = doUpdateArbeidssakForPdl;
		this.avsluttSakProperties = avsluttSakProperties;
	}
	static int i = 0;

	public void oppdaterUtdaterteAktoerIder() {
		log.info("AvsluttAlleSaker starter oppdateringen av utdaterte aktoerId'er");

		List<Long> alleSaksIder = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen(ENDELIGE_STATUSER);
		List<List<Long>> sakIdsPartitioned = Lists.partition(alleSaksIder, BATCHSTOERRELSE);

		sakIdsPartitioned.forEach(partisjon -> {
			doUpdateArbeidssakForPdl.oppdaterUtdaterteAktoerIderForPartisjon(partisjon,i++);
		});
		if(!isEmpty(avsluttSakProperties.getAdministrativEnhet()) && avsluttSakProperties.getAdministrativEnhet().equals("TEST3")) {
			throw new RuntimeException("Crasj app");
		}
		log.info("AvsluttAlleSaker har oppdatert alle utdaterte aktoerId'er");
	}


}
