package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AvsluttAlleSakerService;
import no.nav.dokarkivavlevering.config.AbstractITest;
import no.nav.dokarkivavlevering.core.consumer.nais.exception.TomBodyTexasException;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.Sak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.SakRowMapper;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.assertAvbrutteSaker;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.assertAvsluttetSak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.generateSakParams;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class AvsluttAlleSakerITest extends AbstractITest {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	AvsluttAlleSakerService avsluttAlleSakerService;

	@MockitoBean
	AvsluttSakProperties avsluttSakPropertiesMock;

	private static final long SAK_MED_LUKKET_JOURNALPOST1 = 123L;
	private static final long SAK_MED_LUKKET_JOURNALPOST2 = 234L;
	private static final long SAK_MED_AAPEN_JOURNALPOST = 345L;
	private static final long SAK_UTEN_FERDIGSTILT_JOURNALPOST = 456L;
	private static final long SAK_MED_MASKINELL_JORNALPOST = 567L;
	private static final long SAK_UTEN_DVH_ADMINISTRATIV_ENHET = 678L;
	private static final long SAK_MED_FAGSAKNR1 = 7898L;
	private static final long SAK_MED_FAGSAKNR2 = 7899L;
	private static final long SAK_UTEN_SAKSRELASJONER_ID = 321L;
	private static final String ORGNR = "123456789";
	private static final String FNR = "2345678901234";
	private static final String FNR_OLD = "1234567891123";
	private static final String FNR_NEW = "2345678901234";
	private static final LocalDateTime AVSLUTTET_DATO = LocalDateTime.parse("2025-01-02T15:45");
	private static final String ADMINISTRATIV_ENHET = "Nav Ålesund";

	private static final LocalDateTime JOURNALPOST1_OPPRETTETDATO = LocalDateTime.parse("2025-01-01T13:30:00");
	private static final LocalDateTime JOURNALPOST2_OPPRETTETDATO = LocalDateTime.parse("2025-02-13T14:45:00");

	@BeforeEach
	public void setupAvsluttSakRequiredProperties() {
		when(avsluttSakPropertiesMock.getReferanse()).thenReturn("MMA-1337");
		stubDvh("response.json");
	}

	@Test
	public void skalAvslutteSaker() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, FNR));
		arbeidssakRepository.save(lagSakForOrganisasjon(SAK_MED_LUKKET_JOURNALPOST2, ORGNR));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);
		Arbeidssak arbeidssak2 = arbeidssaker.get(1);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR);
		assertThat(arbeidssak2.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak2.getOrgnr()).isEqualTo(ORGNR);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(123L, 234L)), new SakRowMapper());
		assertAvsluttetSak(saker.get(0), "Nav Lindesnes", JOURNALPOST1_OPPRETTETDATO, now());
		assertAvsluttetSak(saker.get(1), "Nav Buskerud", JOURNALPOST2_OPPRETTETDATO, now());
	}

	@Test
	public void skalBrukeFallbackIDatabaseForAdminEnhet() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_MASKINELL_JORNALPOST, "1234567891234", "FAR"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_MASKINELL_JORNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");

		Sak sak = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(SAK_MED_MASKINELL_JORNALPOST)), new SakRowMapper()).getFirst();
		assertAvsluttetSak(sak, "FAR - FRA DATABASE", JOURNALPOST2_OPPRETTETDATO, now());
	}

	@Test
	public void skalAvslutteSakerMedAvsluttetDato() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		when(avsluttSakPropertiesMock.getAvsluttetDato()).thenReturn(AVSLUTTET_DATO);

		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, FNR));
		arbeidssakRepository.save(lagSakForOrganisasjon(SAK_MED_LUKKET_JOURNALPOST2, ORGNR));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);
		Arbeidssak arbeidssak2 = arbeidssaker.get(1);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR);
		assertThat(arbeidssak2.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak2.getOrgnr()).isEqualTo(ORGNR);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(123L, 234L)), new SakRowMapper());
		assertAvsluttetSak(saker.get(0), "Nav Lindesnes", JOURNALPOST1_OPPRETTETDATO, AVSLUTTET_DATO);
		assertAvsluttetSak(saker.get(1), "Nav Buskerud", JOURNALPOST2_OPPRETTETDATO, AVSLUTTET_DATO);
	}

	@Test
	public void skalAvslutteSakerMedFagsaknr() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		when(avsluttSakPropertiesMock.getAvsluttetDato()).thenReturn(AVSLUTTET_DATO);

		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_FAGSAKNR1, FNR, "FAGSAK_123"));
		arbeidssakRepository.save(lagSakForOrganisasjon(SAK_MED_FAGSAKNR2, ORGNR, "FAGSAK_234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_FAGSAKNR1, SAK_MED_FAGSAKNR2));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);
		Arbeidssak arbeidssak2 = arbeidssaker.get(1);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR);
		assertThat(arbeidssak2.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak2.getOrgnr()).isEqualTo(ORGNR);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(SAK_MED_FAGSAKNR1, SAK_MED_FAGSAKNR2)), new SakRowMapper());
		assertAvsluttetSak(saker.get(0), "Nav Lindesnes", JOURNALPOST1_OPPRETTETDATO, AVSLUTTET_DATO);
		assertAvsluttetSak(saker.get(1), "Nav Lindesnes", JOURNALPOST1_OPPRETTETDATO, AVSLUTTET_DATO);
	}

	@Test
	public void skalAvslutteSakerMedAdministrativEnhet() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		when(avsluttSakPropertiesMock.getAdministrativEnhet()).thenReturn(ADMINISTRATIV_ENHET);

		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, FNR));
		arbeidssakRepository.save(lagSakForOrganisasjon(SAK_MED_LUKKET_JOURNALPOST2, ORGNR));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);
		Arbeidssak arbeidssak2 = arbeidssaker.get(1);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR);
		assertThat(arbeidssak2.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak2.getOrgnr()).isEqualTo(ORGNR);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(123L, 234L)), new SakRowMapper());
		assertAvsluttetSak(saker.get(0), ADMINISTRATIV_ENHET, JOURNALPOST1_OPPRETTETDATO, now());
		assertAvsluttetSak(saker.get(1), ADMINISTRATIV_ENHET, JOURNALPOST2_OPPRETTETDATO, now());
	}

	@Test
	public void skalIkkeAvslutteSakerUtenAdministrativEnhet() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");

		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_DVH_ADMINISTRATIV_ENHET, FNR));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_DVH_ADMINISTRATIV_ENHET));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR);

		Sak sak = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(SAK_UTEN_DVH_ADMINISTRATIV_ENHET)), new SakRowMapper()).getFirst();

		assertThat(sak.saksstatus()).isNull();
		assertThat(sak.datoEndret()).isNull();
	}

	@Test
	public void skalOppdatereStatusTilPdlFantIkkeNyAktoerId() {
		stubTexas();
		stubPdl("hentIdenterBolkSomInneholderNotFound.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, "1234567891123"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();
		Arbeidssak arbeidssak = arbeidssakRepository.findSaksBySakIdIn(List.of(123L)).getFirst();
		assertThat(arbeidssak.getArbeidsstatus()).isEqualTo(FEIL_PDL_FANT_IKKE_AKTOERID);
	}

	@Test
	public void skalOppdatereAktoerIdOgAvslutteSak() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		when(avsluttSakPropertiesMock.getAdministrativEnhet()).thenReturn(ADMINISTRATIV_ENHET);

		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, FNR_OLD));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo(FNR_NEW);

		Sak sak = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(List.of(SAK_MED_LUKKET_JOURNALPOST1)), new SakRowMapper()).getFirst();
		assertAvsluttetSak(sak, ADMINISTRATIV_ENHET, JOURNALPOST1_OPPRETTETDATO, now());
	}

	@Test
	public void skalOppdatereStatusTilFeilAapenJournalpost() {
		Arbeidssak arbeidssakForOrganisasjon = Arbeidssak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();
		arbeidssakRepository.save(arbeidssakForOrganisasjon);
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		Arbeidssak arbeidssak = arbeidssakRepository.findSaksBySakIdIn(List.of(345L)).getFirst();
		assertThat(arbeidssak.getArbeidsstatus()).isEqualTo(FEIL_AAPEN_JOURNALPOST);
	}

	@Test
	public void skalKasteTomBodyTexasExceptionOgAvslutteJobben() {
		stubTexasTomBody();
		populerSakRepository();

		assertThatExceptionOfType(TomBodyTexasException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(123L, 234L, 999L));
		assertThat(arbeidssaker)
				.extracting(Arbeidssak::getArbeidsstatus)
				.containsOnlyNulls();
	}

	@Test
	public void skalKastePdlFunctionalExceptionOgAvslutteJobben() {
		stubTexas();
		stubPdl("validationError.json");
		populerSakRepository();

		assertThatExceptionOfType(PdlFunctionalException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(123L, 234L, 999L));
		assertThat(arbeidssaker)
				.extracting(Arbeidssak::getArbeidsstatus)
				.containsOnlyNulls();
	}

	@Test
	public void skalFeileBehandlingAvArkivsakMedAapenJournalpost() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_AAPEN_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_AAPEN_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_AAPEN_JOURNALPOST);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalFeileBehandlingAvArkivsakUtenJournalfoerendeEnhet() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalAvbryteSakerForTomArkivsak() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_FERDIGSTILT_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_FERDIGSTILT_JOURNALPOST));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_TOM_ARKIVSAK);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakIds);", generateSakParams(SAK_UTEN_FERDIGSTILT_JOURNALPOST), new SakRowMapper());
		assertAvbrutteSaker(saker);
	}

	@Test
	public void skalAvbryteSakerForArkivsakUtenSaksrelasjoner() {
		stubTexas();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_SAKSRELASJONER_ID, "1234567891234"));
		final MapSqlParameterSource sakIdParameterSource = new MapSqlParameterSource().addValue("sakId", SAK_UTEN_SAKSRELASJONER_ID);
		namedParameterJdbcTemplate.update("insert into joark.sak (id) values (:sakId)", sakIdParameterSource);
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_SAKSRELASJONER_ID));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_TOM_ARKIVSAK);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from joark.sak where id in (:sakId);", sakIdParameterSource, new SakRowMapper());
		assertAvbrutteSaker(saker);
	}

	void populerSakRepository() {
		arbeidssakRepository.saveAll(List.of(
				lagSakForAktoer(123L, "1234567891123"),
				lagSakForAktoer(234L, "1234567891234"),
				lagSakForOrganisasjon(999L, "123456789"))
		);

		commitAndBeginNewTransaction();
	}

	private Arbeidssak lagSakForAktoer(Long sakId, String aktoerId, String fagsakNr, String tema) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(fagsakNr)
				.aktoerId(aktoerId)
				.orgnr(null)
				.tema(tema)
				.build();
	}

	private Arbeidssak lagSakForOrganisasjon(Long sakId, String orgnr, String fagsakNr) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(fagsakNr)
				.aktoerId(null)
				.orgnr(orgnr)
				.build();
	}

	private Arbeidssak lagSakForAktoer(Long sakId, String aktoerId) {
		return lagSakForAktoer(sakId, aktoerId, null, "BID");
	}

	private Arbeidssak lagSakForAktoer(Long sakId, String aktoerId, String tema) {
		return lagSakForAktoer(sakId, aktoerId, null, tema);
	}

	private Arbeidssak lagSakForOrganisasjon(Long sakId, String orgnr) {
		return lagSakForOrganisasjon(sakId, orgnr, null);
	}

}