package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse.AdministrativEnhet;
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

	public Optional<String> hentHistoriskNavnForAdministrativEnhet(String journalfoerendeEnhet, LocalDate journalfoertDato, String applikasjon) {
		if (response == null) {
			response = datavarehusConsumer.hentAlleAdministrativeEnheter();
		}

		List<AdministrativEnhet> gyldigeKontorer = response.administrativeEnheter()
				.filter(je -> journalfoerendeEnhet.equals(je.journalfoerendeEnhet()))
				.filter(ae -> varAdministrativEnhetGyldigNaarJournalpostBleJournalfoert(journalfoertDato, ae))
				.toList();

		if (gyldigeKontorer.isEmpty()) {
			return Optional.empty();
		}

		if (gyldigeKontorer.size() == 1) {
			return Optional.of(gyldigeKontorer.getFirst().kontornavn());
		}

		if (APPLIKASJON_IT01.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_INFOTRYGD)) {
			return hentKontornavn(gyldigeKontorer, KONTORTYPE_INFOTRYGD);
		}
		if (APPLIKASJON_AO01.equals(applikasjon) && harDataForKontor(gyldigeKontorer, KONTORTYPE_ARENA)) {
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
		String kontornavn = gyldigeKontorer.stream()
				.filter(ae -> ae.kontortype().equals(fagsystem))
				.findFirst()
				.get().kontornavn();

		return Optional.of(kontornavn);
	}

	private boolean harDataForKontor(List<AdministrativEnhet> gyldigeKontorer, String fagsystem) {
		return gyldigeKontorer.stream()
				.anyMatch(ae -> ae.kontortype().equals(fagsystem));
	}
}
