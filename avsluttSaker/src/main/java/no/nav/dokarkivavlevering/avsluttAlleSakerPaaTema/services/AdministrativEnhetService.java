package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse.AdministrativEnhet;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions.KanIkkeBehandleArkivsakException;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.oppdaterArbeidsstatusForArkivsak;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AdministrativEnhetService {

	private Map<String, List<AdministrativEnhet>> administrativEnhetMap;
	private final DatavarehusConsumer datavarehusConsumer;

	public static final String APPLIKASJON_INFOTRYGD = "IT01";
	public static final String APPLIKASJON_ARENA = "AO01";
	public static final String KONTORTYPE_NORG = "NORGENHET";
	public static final String KONTORTYPE_INFOTRYGD = "INFOENHET";
	public static final String KONTORTYPE_ARENA = "ARENAENHET";

	public AdministrativEnhetService(DatavarehusConsumer datavarehusConsumer) {
		this.datavarehusConsumer = datavarehusConsumer;
	}

	@Async
	@EventListener(ContextRefreshedEvent.class)
	public void populerAdministrativEnhetMap() {
		log.info("Populerer administrativEnhetMap med data fra datavarehus");
		List<AdministrativEnhet> administrativEnheter = datavarehusConsumer.hentAlleAdministrativeEnheter().getItems();
		administrativEnhetMap = administrativEnheter.stream()
				.collect(Collectors.groupingBy(AdministrativEnhet::journalfoerendeEnhet));
		log.info("Fant {} administrative enheter fra datavarehus", administrativEnhetMap.size());
	}

	public String hentHistoriskNavnForAdministrativEnhet(Journalpost eldsteJournalpost, Arkivsak arkivsak) {
		Optional<String> administrativEnhetOptional = finnHistoriskKontornavnForAdministrativEnhet(
				eldsteJournalpost.getJournalfoerendeEnhet(), eldsteJournalpost.getJournaldato(), arkivsak.getApplikasjon());

		if (administrativEnhetOptional.isEmpty()) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK);
			throw new KanIkkeBehandleArkivsakException(format("Fant ingen administrativ enhet for arkivsak med saksIder=%s", arkivsak.getArbeidssaksIder()));
		}
		return administrativEnhetOptional.get();
	}

	private Optional<String> finnHistoriskKontornavnForAdministrativEnhet(String journalfoerendeEnhet, LocalDate journalfoertDato, String applikasjon) {

		List<AdministrativEnhet> kontorer = administrativEnhetMap.get(journalfoerendeEnhet);
		if (kontorer == null) {
			return Optional.empty();
		}

		List<AdministrativEnhet> gyldigeKontorer = kontorer.stream()
				.filter(ae -> varAdministrativEnhetGyldigNaarJournalpostBleJournalfoert(journalfoertDato, ae))
				.toList();

		if (gyldigeKontorer.isEmpty()) {
			return Optional.empty();
		}

		if (gyldigeKontorer.size() == 1) {
			return Optional.of(gyldigeKontorer.getFirst().kontornavn());
		}

		if (APPLIKASJON_INFOTRYGD.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_INFOTRYGD)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_INFOTRYGD);
		}
		if (APPLIKASJON_ARENA.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_ARENA)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_ARENA);
		}

		if (harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_NORG);
		}

		if (!harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG) && harDataForKontor(gyldigeKontorer, KONTORTYPE_INFOTRYGD)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_INFOTRYGD);
		}

		if (!harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG) && harDataForKontor(gyldigeKontorer, KONTORTYPE_ARENA)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_ARENA);
		}

		return Optional.empty();
	}

	private static boolean varAdministrativEnhetGyldigNaarJournalpostBleJournalfoert(LocalDate journalfoertDato, AdministrativEnhet administrativEnhet) {
		return erDatoLikEllerEtter(journalfoertDato, administrativEnhet.gyldigFraDato()) && erDatoLikEllerFoer(journalfoertDato, administrativEnhet.gyldigTilDato());
	}

	private static boolean erDatoLikEllerFoer(LocalDate journalfoertDato, LocalDate gyldigTilDato) {
		return journalfoertDato.isEqual(gyldigTilDato) || journalfoertDato.isBefore(gyldigTilDato);
	}

	private static boolean erDatoLikEllerEtter(LocalDate journalfoertDato, LocalDate gyldigFraDato) {
		return journalfoertDato.isEqual(gyldigFraDato) || journalfoertDato.isAfter(gyldigFraDato);
	}

	private Optional<String> hentKontornavn(List<AdministrativEnhet> gyldigeKontorer, String fagsystem) {
		return gyldigeKontorer.stream()
				.filter(ae -> fagsystem.equals(ae.kontortype()))
				.map(AdministrativEnhet::kontornavn)
				.findFirst();
	}

	private boolean harDataForKontor(List<AdministrativEnhet> gyldigeKontorer, String fagsystem) {
		return gyldigeKontorer.stream()
				.anyMatch(ae -> ae.kontortype().equals(fagsystem));
	}
}
