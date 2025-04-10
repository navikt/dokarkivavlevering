package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusResponse;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusResponse.AdministrativEnhet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AdministrativEnhetService {

	private final DatavarehusConsumer datavarehusConsumer;
	private DatavarehusResponse response = null;

	public static final String APPLIKASJON_IT01 = "IT01";
	public static final String APPLIKASJON_AO01 = "AO01";
	public static final String KONTORTYPE_NORG = "NORGENHET";
	public static final String KONTORTYPE_INFOTRYGD = "INFOENHET";
	public static final String KONTORTYPE_ARENA = "ARENAENHET";

	public AdministrativEnhetService(DatavarehusConsumer datavarehusConsumer) {
		this.datavarehusConsumer = datavarehusConsumer;
	}

	public Optional<String> hentHistoriskNavnForAdministrativEnhet(String journalfoerendeEnhet, LocalDate journalFoertDato, String applikasjon) {
		if (response == null) {
			response = datavarehusConsumer.hentAlleAdministrativeEnheter();
		}

		List<AdministrativEnhet> gyldigeKontorer = response.administrativeEnheter()
				.filter(je -> journalfoerendeEnhet.equals(je.journalfoerendeEnhet()))
				.filter(ae -> varAdministrativEnhetGyldigNarJournalpostBleJournalfoert(journalFoertDato, ae))
				.toList();

		if (gyldigeKontorer.isEmpty()) {
			return Optional.empty();
		}
		if (gyldigeKontorer.size() == 1) {
			return Optional.of(gyldigeKontorer.getFirst().kontornavn());
		}
		if (APPLIKASJON_IT01.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_INFOTRYGD)) {
			return getKontornavn(gyldigeKontorer, KONTORTYPE_INFOTRYGD);
		}
		if (APPLIKASJON_AO01.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_ARENA)) {
			return getKontornavn(gyldigeKontorer, KONTORTYPE_ARENA);
		}
		if (harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG)) {
			return getKontornavn(gyldigeKontorer, KONTORTYPE_NORG);
		}
		if (!harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG) && harDataForKontor(gyldigeKontorer, KONTORTYPE_INFOTRYGD)) {
			return getKontornavn(gyldigeKontorer, KONTORTYPE_INFOTRYGD);
		}
		if (!harDataForKontor(gyldigeKontorer, KONTORTYPE_NORG) && harDataForKontor(gyldigeKontorer, KONTORTYPE_ARENA)) {
			return getKontornavn(gyldigeKontorer, KONTORTYPE_ARENA);
		}

		return Optional.empty();
	}

	private static boolean varAdministrativEnhetGyldigNarJournalpostBleJournalfoert(LocalDate journalFoertDato, AdministrativEnhet ae) {
		return erDatoLikEllerEtter(journalFoertDato, ae.gyldigFraDato()) && erDatoLikEllerFoer(journalFoertDato, ae.gyldigTilDato());
	}

	private static boolean erDatoLikEllerFoer(LocalDate journalFoertDato, LocalDate gyldigTilDato) {
		return journalFoertDato.isEqual(gyldigTilDato) || journalFoertDato.isBefore(gyldigTilDato);
	}

	private static boolean erDatoLikEllerEtter(LocalDate journalFoertDato, LocalDate gyldigFraDato) {
		return journalFoertDato.isEqual(gyldigFraDato) || journalFoertDato.isAfter(gyldigFraDato);
	}

	private Optional<String> getKontornavn(List<AdministrativEnhet> gyldigKontorer, String fagsystem) {
		return Optional.of(gyldigKontorer.stream().filter(ae -> ae.kontortype().equals(fagsystem)).findFirst().get().kontornavn());
	}

	private boolean harDataForKontor(List<AdministrativEnhet> gyldigKontorer, String fagsystem) {
		return gyldigKontorer.stream().anyMatch(ae -> ae.kontortype().equals(fagsystem));
	}
}
