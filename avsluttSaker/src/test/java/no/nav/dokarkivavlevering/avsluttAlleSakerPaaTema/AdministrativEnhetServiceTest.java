package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers.DatavarehusResponse.AdministrativEnhet;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions.KanIkkeBehandleArkivsakException;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService.APPLIKASJON_ARENA;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService.APPLIKASJON_INFOTRYGD;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService.KONTORTYPE_ARENA;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService.KONTORTYPE_INFOTRYGD;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AdministrativEnhetService.KONTORTYPE_NORG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
	public void skalReturnereAdministrativEnhetNaarDvhReturnererEtSvar() {
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(new DatavarehusResponse(List.of(createDefaultAdministrativEnhet())));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_GOSYS);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontornavnForInfotrygd(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_INFOTRYGD, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_INFOTRYGD);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontornavnForArena(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_ARENA);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontornavnNorgDersomApplikasjonManglerIDvhRespons(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_GOSYS);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontornavnInfotrygdDersomApplikasjonOgNorgManglerIDvhRespons(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_INFOTRYGD, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_GOSYS);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalReturnereKontornavnArenaDersomApplikasjonOgNorgOgInfotrygdManglerIDvhRespons(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, KONTORTYPE_ARENA, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR", KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_GOSYS);

		String administrativEnhet = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak);
		assertThat(administrativEnhet).isEqualTo(KONTORNAVN_OSLO);
	}

	@Test
	public void skalIkkeFinneKontornavnNaarIngenMatchendeKontortyperFraDvh(){
		when(datavarehusConsumermock.hentAlleAdministrativeEnheter()).thenReturn(
				new DatavarehusResponse(List.of(
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR2", KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL),
						createAdministrativEnhet(JFR_ENHET, "FIKTIVT_KONTOR", KONTORNAVN_KRISTIANIA, GYLDIG_FRA, GYLDIG_TIL))));
		administrativEnhetService.populerAdministrativEnhetMap();
		Journalpost journalpost = lagJournalpost();
		Arkivsak arkivsak = lagArkivsakMedApplikasjon(APPLIKASJON_GOSYS);

		assertThatExceptionOfType(KanIkkeBehandleArkivsakException.class)
				.isThrownBy(() -> administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(journalpost, arkivsak))
				.withMessageContaining("Fant ingen administrativ enhet for arkivsak");
	}

	private AdministrativEnhet createDefaultAdministrativEnhet() {
		return createAdministrativEnhet(JFR_ENHET, KONTORTYPE_NORG, KONTORNAVN_OSLO, GYLDIG_FRA, GYLDIG_TIL);
	}

	private AdministrativEnhet createAdministrativEnhet(String journalfoerendeEnhet, String kontortype, String kontornavn, LocalDate gyldigFra, LocalDate gyldigTil) {
		return new AdministrativEnhet(
				journalfoerendeEnhet,
				kontortype,
				kontornavn,
				gyldigFra,
				gyldigTil
		);
	}

	private Journalpost lagJournalpost() {
		return Journalpost.builder()
				.journalfoerendeEnhet(JFR_ENHET)
				.journaldato(GYLDIG_FRA)
				.build();
	}

	private Arkivsak lagArkivsakMedApplikasjon(String applikasjon) {
		Arbeidssak arbeidssak = new Arbeidssak();
		arbeidssak.setApplikasjon(applikasjon);
		List<Arbeidssak> arbeidssaker = List.of(arbeidssak);

		return new Arkivsak(arbeidssaker, null);
	}

}