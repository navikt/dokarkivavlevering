package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions.KanIkkeBehandleArkivsakException;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.ENDELIGE_STATUSER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.grupperArbeidssakerPerAktoerId;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.grupperArbeidssakerPerOrgnr;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.oppdaterArbeidsstatusForArkivsak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.ArkivsakValidator.harArkivsakEnAapenJournalpost;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.ArkivsakValidator.harArkivsakFerdigstilteJournalposter;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@Transactional
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private final ArbeidssakRepository arbeidssakRepository;
	private final AvsluttSakRepository avsluttSakRepository;
	private final AvsluttSakProperties avsluttSakProperties;
	private final OppdaterAktoerIdService oppdaterAktoerIdService;
	private final JournalpostService journalpostService;
	private final AdministrativEnhetService administrativEnhetService;

	public AvsluttAlleSakerService(ArbeidssakRepository arbeidssakRepository,
								   AvsluttSakRepository avsluttSakRepository,
								   AvsluttSakProperties avsluttSakProperties,
								   OppdaterAktoerIdService oppdaterAktoerIdService,
								   JournalpostService journalpostService,
								   AdministrativEnhetService administrativEnhetService) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.avsluttSakRepository = avsluttSakRepository;
		this.avsluttSakProperties = avsluttSakProperties;
		this.oppdaterAktoerIdService = oppdaterAktoerIdService;
		this.journalpostService = journalpostService;
		this.administrativEnhetService = administrativEnhetService;
	}

	public void avsluttAlleSaker() {
		oppdaterAktoerIdService.oppdaterUtdaterteAktoerIder();

		avsluttAlleSakerForAktoerId();
		avsluttAlleSakerOrgnr();
	}

	private void avsluttAlleSakerForAktoerId(){
		List<String> alleAktoerIder = arbeidssakRepository.findDistinctAktoerIds(ENDELIGE_STATUSER);
		//Del alle aktørId'ene opp i håndterlige partisjoner
		List<List<String>> aktoerIdsPartitioned = Lists.partition(alleAktoerIder, 200);

		for (List<String> aktoerIdList : aktoerIdsPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByAktoerIdIn(aktoerIdList);

			List<List<Arbeidssak>> groupedLists = grupperArbeidssakerPerAktoerId(arbeidssaker);

			List<Arkivsak> arkivsaker = lagArkivsaker(groupedLists);
			arkivsaker.forEach(this::avsluttSak);
		}
	}

	private void avsluttAlleSakerOrgnr(){
		List<String> alleOrgNr = arbeidssakRepository.findDistinctOrgnrs(ENDELIGE_STATUSER);
		List<List<String>> orgnrPartitioned = Lists.partition(alleOrgNr, 200);

		for (List<String> orgNrList : orgnrPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByOrgnrIn(orgNrList);

			List<List<Arbeidssak>> groupedLists = grupperArbeidssakerPerOrgnr(arbeidssaker);

			List<Arkivsak> arkivsaker = lagArkivsaker(groupedLists);
			arkivsaker.forEach(this::avsluttSak);
		}
	}

	private List<Arkivsak> lagArkivsaker(List<List<Arbeidssak>> arbeidssaksListe) {
		ArrayList<Arkivsak> arkivsaker = new ArrayList<>();
		for (List<Arbeidssak> arbsaker : arbeidssaksListe) {
			Arkivsak arkivsak = new Arkivsak(arbsaker, new ArrayList<>());
			List<Journalpost> tilhoerendeJournalposter = journalpostService.hentTilhoerendeJournalposter(arkivsak);
			arkivsak.journalposter().addAll(tilhoerendeJournalposter);
			arkivsaker.add(arkivsak);
		}
		return arkivsaker;
	}

	private void avsluttSak(Arkivsak arkivsak) {
		try {
			validerAtArkivsakenSkalAvsluttes(arkivsak);

			Journalpost eldsteJournalpost = journalpostService.finnEldsteJournalpostForArkivsak(arkivsak);

			String administrativEnhet = avsluttSakProperties.getAdministrativEnhet();
			if (isEmpty(administrativEnhet)) {
				administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(eldsteJournalpost, arkivsak);
			}

			LocalDateTime datoSakOpprettet = eldsteJournalpost.getOpprettetdato();
			avsluttSakRepository.avsluttSaker(arkivsak.getArbeidssaksIder(), hentDatoAvsluttet(), datoSakOpprettet, administrativEnhet);
			oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_SAK_AVSLUTTET);

		} catch (KanIkkeBehandleArkivsakException e) {
			log.warn(e.getMessage());
		}
	}

	private void validerAtArkivsakenSkalAvsluttes(Arkivsak arkivsak) {
		avbrytArkivsakDersomArkivsakenHarAapneJournalposter(arkivsak);
		avbrytArkivsakDersomArkivsakenErTom(arkivsak);
	}

	private void avbrytArkivsakDersomArkivsakenHarAapneJournalposter(Arkivsak arkivsak) {
		if (harArkivsakEnAapenJournalpost(arkivsak.journalposter())) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_AAPEN_JOURNALPOST);
			throw new KanIkkeBehandleArkivsakException(format("Kan ikke avslutte arkivsak med åpne journalposter for saksIder=%s", arkivsak.getArbeidssaksIder()));
		}
	}

	private void avbrytArkivsakDersomArkivsakenErTom(Arkivsak arkivsak) {
		if (!harArkivsakFerdigstilteJournalposter(arkivsak.journalposter())) {
			avsluttSakRepository.avbrytSaker(arkivsak.getArbeidssaksIder());
			oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_TOM_ARKIVSAK);
			throw new KanIkkeBehandleArkivsakException(format("Arkivsak har ingen ferdigstilte journalposter. Avbryter saker=%s knyttet til tom arkivsak.", arkivsak.getArbeidssaksIder()));
		}
	}

	private LocalDateTime hentDatoAvsluttet() {
		return avsluttSakProperties.getAvsluttetDato() != null ? avsluttSakProperties.getAvsluttetDato() : now();
	}

}