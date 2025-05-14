package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private final ArbeidssakRepository arbeidssakRepository;
	private final AvsluttSakProperties avsluttSakProperties;
	private final OppdaterAktoerIdService oppdaterAktoerIdService;
	private final AdministrativEnhetService administrativEnhetService;
	private final AvsluttAlleSakerDoUpdates avsluttAlleSakerDoUpdates;

	public AvsluttAlleSakerService(ArbeidssakRepository arbeidssakRepository,
								   AvsluttSakProperties avsluttSakProperties,
								   OppdaterAktoerIdService oppdaterAktoerIdService,
								   AdministrativEnhetService administrativEnhetService,
								   AvsluttAlleSakerDoUpdates avsluttAlleSakerDoUpdates) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.avsluttSakProperties = avsluttSakProperties;
		this.oppdaterAktoerIdService = oppdaterAktoerIdService;
		this.administrativEnhetService = administrativEnhetService;
		this.avsluttAlleSakerDoUpdates = avsluttAlleSakerDoUpdates;
	}

	@Transactional
	public void avsluttAlleSaker() {
		if (isEmpty(avsluttSakProperties.getAdministrativEnhet())) {
			administrativEnhetService.populerAdministrativEnhetMap();
		}
		oppdaterAktoerIdService.oppdaterUtdaterteAktoerIder();

		avsluttAlleSakerForAktoerId();
		avsluttAlleSakerOrgnr();
		loggOppsummering();
	}

	private void loggOppsummering() {
		Map<Arbeidsstatus, Long> arbeidsstatusMap = hentOgMapAntallArbeidssakerIHverArbeidstatus();

		arbeidsstatusMap.forEach(((key, value) -> log.info("Arbeidsstatus={} har antallSaker={} ", key.name(), value)));
	}

	public void avsluttAlleSakerForAktoerId() {
		List<String> alleAktoerIder = arbeidssakRepository.hentOppdaterteAktoerIder();
		//Del alle aktørId'ene opp i håndterlige partisjoner
		List<List<String>> aktoerIdsPartitioned = Lists.partition(alleAktoerIder, 200);

		for (List<String> aktoerIdList : aktoerIdsPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			avsluttAlleSakerDoUpdates.avsluttAktoerIdSakerForPartisjon(aktoerIdList);
		}
	}

	public void avsluttAlleSakerOrgnr() {
		List<String> alleOrgNr = arbeidssakRepository.hentOrgnrs();
		List<List<String>> orgnrPartitioned = Lists.partition(alleOrgNr, 200);

		for (List<String> orgNrList : orgnrPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			avsluttAlleSakerDoUpdates.avsluttOrgnrSakerForPartisjon(orgNrList);
		}
	}

	public Map<Arbeidsstatus, Long> hentOgMapAntallArbeidssakerIHverArbeidstatus() {
		return arbeidssakRepository.tellAntallArbeidssakerForHverArbeidsstatus().stream()
				.collect(Collectors.toMap(
						row -> (Arbeidsstatus) row[0], // arbeidsstatus
						row -> (Long) row[1]    // count
				));
	}
}