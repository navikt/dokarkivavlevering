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
		List<Long> alleSaksIder = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen();
		List<List<Long>> sakIdsPartitioned = Lists.partition(alleSaksIder, BATCHSTOERRELSE);

		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterAlleAktoerIder(arbeidssaker);
		});
	}

	private void oppdaterAlleAktoerIder(List<Arbeidssak> saker) {
		List<Arbeidssak> sakerMedOrgnr = saker.stream()
				.filter(arbeidssak -> arbeidssak.getOrgnr() != null)
				.toList();
		sakerMedOrgnr.forEach(arbeidssak -> arbeidssak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL));

		List<Arbeidssak> sakerMedAktoerId = saker.stream()
				.filter(arbeidssak -> arbeidssak.getAktoerId() != null)
				.toList();

		Set<String> aktoerIds = sakerMedAktoerId.stream()
				.map(Arbeidssak::getAktoerId)
				.collect(Collectors.toSet());

		if (!aktoerIds.isEmpty()) {
			oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(aktoerIds, sakerMedAktoerId);
		}
	}

	private void oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(Set<String> aktoerIds, List<Arbeidssak> arbeidssakerMedAktoerId) {
		List<HentIdenterBolk> hentIdenterBolkListe = pdlGraphQLConsumer.hentGjeldendeAktoerIder(aktoerIds);
		Map<String, String> aktoerIderSomSkalOppdateres = new HashMap<>();
		List<String> aktoerIderUtenGyldigAktoerId = new ArrayList<>();

		hentIdenterBolkListe.forEach(identBolk -> {
			if (OK.equals(identBolk.getCode())) {
				// key er gammel aktoerId, value er ny aktoerId
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

}
