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
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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
		if (isEmpty(avsluttSakProperties.getAdministrativEnhet())) {
			administrativEnhetService.populerAdministrativEnhetMap();
		}
		oppdaterAktoerIdService.oppdaterUtdaterteAktoerIder();

		avsluttAlleSakerForAktoerId();
		avsluttAlleSakerOrgnr();
	}

	public void avsluttAlleSakerForAktoerId() {
		List<String> alleAktoerIder = arbeidssakRepository.findDistinctAktoerIds(ENDELIGE_STATUSER);
		//Del alle aktørId'ene opp i håndterlige partisjoner
		List<List<String>> aktoerIdsPartitioned = Lists.partition(alleAktoerIder, 200);

		for (List<String> aktoerIdList : aktoerIdsPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			avsluttAktoerIdSakerForPartisjon(aktoerIdList);
		}
	}

	@Transactional(propagation = REQUIRES_NEW)
	public void avsluttAktoerIdSakerForPartisjon(List<String> aktoerIdList) {
		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByAktoerIdIn(aktoerIdList);

		List<List<Arbeidssak>> arbeidssakerPerAktoerId = grupperArbeidssakerPerAktoerId(arbeidssaker);

		List<Arkivsak> arkivsaker = lagArkivsaker(arbeidssakerPerAktoerId);
		log.info("Skal avslutte arkivsaker={}", arkivsaker);
		arkivsaker.forEach(this::avsluttSak);
	}

	public void avsluttAlleSakerOrgnr() {
		List<String> alleOrgNr = arbeidssakRepository.findDistinctOrgnrs(ENDELIGE_STATUSER);
		List<List<String>> orgnrPartitioned = Lists.partition(alleOrgNr, 200);

		for (List<String> orgNrList : orgnrPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			avsluttOrgnrSakerForPartisjon(orgNrList);
		}
	}

	@Transactional(propagation = REQUIRES_NEW)
	public void avsluttOrgnrSakerForPartisjon(List<String> orgNrList) {
		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByOrgnrIn(orgNrList);

		List<List<Arbeidssak>> arbeidssakerPerOrgNr = grupperArbeidssakerPerOrgnr(arbeidssaker);

		List<Arkivsak> arkivsaker = lagArkivsaker(arbeidssakerPerOrgNr);
		log.info("Skal avslutte arkivsaker={}", arkivsaker);
		arkivsaker.forEach(this::avsluttSak);
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
			LocalDateTime datoSakOpprettet = eldsteJournalpost.getOpprettetdato();
			String administrativEnhet = bestemAdministrativEnhet(eldsteJournalpost, arkivsak);
			//TODO: Expand column! !! !!! !!!! !!!!! !!!!!!! !!!!!!!! !!!!!!!!!!
			if(administrativEnhet.length() > 40) {
				log.info("Mottok for lang Administrativ Enhet: " + administrativEnhet + " Med lengde: " + administrativEnhet.length());
				administrativEnhet = administrativEnhet.substring(0, 40);
			}
			avsluttSakRepository.avsluttSaker(arkivsak.getArbeidssaksIder(), hentDatoAvsluttet(), datoSakOpprettet, administrativEnhet);
			oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_SAK_AVSLUTTET);
			log.info("Har avsluttet arkivsak med saksIder={}", arkivsak.getArbeidssaksIder());

		} catch (KanIkkeBehandleArkivsakException e) {
			log.warn("Feilet i å avslutte arkivsak med saksIds={} med feilmelding={}", arkivsak.getArbeidssaksIder(), e.getMessage(), e);
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

	private String bestemAdministrativEnhet(Journalpost eldsteJournalpost, Arkivsak arkivsak) {
		return isEmpty(avsluttSakProperties.getAdministrativEnhet()) ? administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(eldsteJournalpost, arkivsak) : avsluttSakProperties.getAdministrativEnhet();
	}

	private LocalDateTime hentDatoAvsluttet() {
		return avsluttSakProperties.getAvsluttetDato() != null ? avsluttSakProperties.getAvsluttetDato() : now();
	}

}