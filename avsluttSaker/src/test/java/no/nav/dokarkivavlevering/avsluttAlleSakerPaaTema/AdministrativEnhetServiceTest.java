package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse.AdministrativEnhet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AdministrativEnhetService.APPLIKASJON_AO01;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AdministrativEnhetService.APPLIKASJON_IT01;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AdministrativEnhetService.KONTORTYPE_ARENA;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AdministrativEnhetService.KONTORTYPE_INFOTRYGD;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AdministrativEnhetService.KONTORTYPE_NORG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativEnhetServiceTest {

	@Mock
	private DatavarehusConsumer datavarehusConsumermock;

	@InjectMocks
	AdministrativEnhetService administrativEnhetService;

	private static final String JFR_ENHET = "1234";
	private static final String KONTORNAVN_OSLO = "OSLO";
	private static final String KONTORNAVN_KRISTIANIA = "KRISTIANIA";
	private static final String APPLIKASJON_GOSYS = "FS22";
	private static final LocalDate GYLDIG_FRA = LocalDate.of(2020, 1, 1);
	private static final LocalDate GYLDIG_TIL = GYLDIG_FRA.plusYears(2);

	@Test
	public void skalIkkeFinneAdministrativEnhetNarDvhReturnerernullListe() {
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(new DatavarehusResponse());

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, LocalDate.now(), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet).isEmpty();
	}
	@Test
	public void skalIkkeFinneAdministrativEnhetNarDvhReturnererTomListe() {
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(new DatavarehusResponse(emptyList()));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, LocalDate.now(), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet).isEmpty();
	}

	@Test
	public void skalReturnereAdministrativEnhetNarDvhReturnererEtSvar() {
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(new DatavarehusResponse(List.of(createDefaultAdministrativEnhet())));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA, APPLIKASJON_GOSYS);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereAdministrativEnhetForIT01(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_INFOTRYGD, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_IT01);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereAdministrativEnhetForAO01(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_AO01);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereAdministrativEnhetUtenApplikasjon(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontorNavnForInfotrygdAdministrativEnhetUtenApplikasjonOgUtenNorg(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_INFOTRYGD, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontorNavnForArenaAdministrativEnhetUtenApplikasjonOgUtenNorg(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR", KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet.get()).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalIkkeFinneAdministrativEnhetNarIngenGyldigeKontortyperFraDvh(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR2", KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR", KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));

		Optional<String> administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(JFR_ENHET, GYLDIG_FRA.plusDays(5), APPLIKASJON_GOSYS);
		assertThat(administrativEnhet).isEmpty();
	}

	private AdministrativEnhet createDefaultAdministrativEnhet() {
		return createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL);
	}

	private AdministrativEnhet createAdministrativEnhet(String journalfoerendeEnhet, String kontortype, String kontornavn, LocalDate gyldigFra, LocalDate GyldigTil) {
		return new AdministrativEnhet(
				journalfoerendeEnhet,
				kontortype,
				kontornavn,
				gyldigFra,
				GyldigTil
		);
	}

}