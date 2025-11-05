package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions.KanIkkeBehandleArkivsakException;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.oppdaterArbeidsstatusForArkivsak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.ArkivsakValidator.LUKKEDE_JOURNALSTATUSER;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Profile("avsluttSaker")
public class JournalpostService {

	private static final String MASKINELL_JOURNALFOERENDE_ENHET = "9999";
	private final AvsluttSakRepository avsluttSakRepository;
	private static String inputAdministrativEnhet;

	public JournalpostService(AvsluttSakProperties avsluttSakProperties,
							  AvsluttSakRepository avsluttSakRepository) {
		this.avsluttSakRepository = avsluttSakRepository;
		inputAdministrativEnhet = avsluttSakProperties.getAdministrativEnhet();
	}

	public List<Journalpost> hentTilhoerendeJournalposter(Arkivsak arkivsak) {
		return avsluttSakRepository.getJournalposterForArkivsak(arkivsak.getArbeidssaksIder());
	}

	public Journalpost finnEldsteJournalpostForArkivsak(Arkivsak arkivsak) {
		Optional<Journalpost> eldsteJournalpostOptional = finnEldsteJournalpost(arkivsak);
		if (eldsteJournalpostOptional.isEmpty()) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET);
			throw new KanIkkeBehandleArkivsakException(format("Fant ingen journalposter i gyldig status med journalforendeEnhet for saksIder=%s. Kan ikke bestemme administrativEnhet.", arkivsak.getArbeidssaksIder()));
		}
		return eldsteJournalpostOptional.get();
	}

	private Optional<Journalpost> finnEldsteJournalpost(Arkivsak arkivsak) {
		List<Journalpost> filtrerteJournalposter = arkivsak.journalposter().stream()
				.filter(journalpost -> LUKKEDE_JOURNALSTATUSER.contains(journalpost.getJournalstatus()))
				.filter(journalpost -> !journalpost.isErFeilregistrert())
				.filter(journalpost -> !isEmpty(inputAdministrativEnhet) || harGyldigJournalfoerendeEnhet(journalpost))
				.toList();

		if (filtrerteJournalposter.isEmpty()) {
			return Optional.empty();
		}
		return filtrerteJournalposter.stream()
				.min(Comparator.comparing(Journalpost::getJournaldato));
	}

	private boolean harGyldigJournalfoerendeEnhet(Journalpost journalpost){
		return (!isEmpty(journalpost.getJournalfoerendeEnhet())
						&& !MASKINELL_JOURNALFOERENDE_ENHET.equals(journalpost.getJournalfoerendeEnhet()));
	}
}
