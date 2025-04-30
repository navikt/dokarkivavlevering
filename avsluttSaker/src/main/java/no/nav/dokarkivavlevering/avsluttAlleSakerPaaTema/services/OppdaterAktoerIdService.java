package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.HENTET_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.SKAL_IKKE_HENTE_FRA_PDL;

@Slf4j
@Service
@Profile("avsluttSaker")
public class OppdaterAktoerIdService {

	private static final String OK = "ok";
	private static final int BATCHSTOERRELSE = 1000;

	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final ArbeidssakRepository arbeidssakRepository;

	public OppdaterAktoerIdService(PdlGraphQLConsumer pdlGraphQLConsumer,
								   ArbeidssakRepository arbeidssakRepository) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.arbeidssakRepository = arbeidssakRepository;
	}

	public void oppdaterUtdaterteAktoerIder() {
		log.info("AvsluttAlleSaker starter oppdateringen av utdaterte aktoerId'er");
		List<Long> alleSaksIder = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen();
		List<List<Long>> sakIdsPartitioned = Lists.partition(alleSaksIder, BATCHSTOERRELSE);

		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			log.info("Starter oppdatering av utdaterte aktoerId'er for de neste {} sakene", sakIdListe.size());
			oppdaterAlleAktoerIder(arbeidssaker);
			log.info("Har oppdatert utdaterte aktoerId'er");
		});
		log.info("AvsluttAlleSaker har oppdatert alle utdaterte aktoerId'er");
	}

	private void oppdaterAlleAktoerIder(List<Arbeidssak> saker) {
		List<Arbeidssak> sakerMedOrgnr = saker.stream()
				.filter(arbeidssak -> arbeidssak.getOrgnr() != null)
				.toList();
		sakerMedOrgnr.forEach(arbeidssak -> arbeidssak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL));

		List<Arbeidssak> sakerMedAktoerId = saker.stream()
				.filter(arbeidssak -> arbeidssak.getAktoerId() != null)
				.toList();

		if (!sakerMedAktoerId.isEmpty()) {
			oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(sakerMedAktoerId);
		}
	}

	private void oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(List<Arbeidssak> arbeidssakerMedAktoerId) {

		Set<String> aktoerIderFraArbeidssaker = hentAktoerIderFraArbeidssaker(arbeidssakerMedAktoerId);
		List<HentIdenterBolk> hentIdenterBolkListe = pdlGraphQLConsumer.hentGjeldendeAktoerIder(aktoerIderFraArbeidssaker);
		Map<String, String> aktoerIderSomSkalOppdateres = new HashMap<>();
		List<String> aktoerIderUtenGyldigAktoerId = new ArrayList<>();

		hentIdenterBolkListe.forEach(identBolk -> {
			if (OK.equals(identBolk.getCode())) {
				String gammelAktoerId = identBolk.getIdent();
				String nyAktoerId = identBolk.getIdenter().getFirst().getIdent();

				if (!gammelAktoerId.equals(nyAktoerId)) {
					aktoerIderSomSkalOppdateres.put(identBolk.getIdent(), identBolk.getIdenter().getFirst().getIdent());
				}
			} else {
				aktoerIderUtenGyldigAktoerId.add(identBolk.getIdent());
			}
		});

		arbeidssakerMedAktoerId.forEach(arbeidssak -> {
			if (aktoerIderUtenGyldigAktoerId.contains(arbeidssak.getAktoerId())) {
				log.warn("Feil ved uthenting av person fra pdl. Sak={}", arbeidssak.getSakId());
				arbeidssak.setArbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID);
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(arbeidssak.getAktoerId())) {
					arbeidssak.setAktoerId(aktoerIderSomSkalOppdateres.get(arbeidssak.getAktoerId()));
				}
				arbeidssak.setArbeidsstatus(HENTET_FRA_PDL);
			}
		});
	}

	private Set<String> hentAktoerIderFraArbeidssaker(List<Arbeidssak> sakerMedAktoerId) {
		return sakerMedAktoerId.stream()
				.map(Arbeidssak::getAktoerId)
				.collect(Collectors.toSet());
	}
}
