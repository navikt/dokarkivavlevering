package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AvsluttAlleSakerService;
import no.nav.dokarkivavlevering.config.AbstractITest;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.Sak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.SakRowMapper;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.assertAvbrutteSaker;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.assertAvsluttetSak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryTestUtils.generateSakParams;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;
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

	private static final Long SAK_MED_LUKKET_JOURNALPOST1 = 123L;
	private static final Long SAK_MED_LUKKET_JOURNALPOST2 = 234L;
	private static final Long SAK_MED_AAPEN_JOURNALPOST = 345L;
	private static final Long SAK_UTEN_FERDIGSTILT_JOURNALPOST = 456L;
	private static final Long SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST = 567L;
	private static final Long SAK_UTEN_DVH_ADMINISTRATIV_ENHET = 678L;
	private static final Long SAK_MED_FAGSAKNR1 = 7898L;
	private static final Long SAK_MED_FAGSAKNR2 = 7899L;
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
	}

	//@Test
	public void skalAvslutteSaker() {
		stubAzure();
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

	//@Test
	public void skalAvslutteSakerMedAvsluttetDato() {
		stubAzure();
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

	//@Test
	public void skalAvslutteSakerMedFagsaknr() {
		stubAzure();
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

	//@Test
	public void skalAvslutteSakerMedAdministrativEnhet() {
		stubAzure();
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

	//@Test
	public void skalIkkeAvslutteSakerUtenAdministrativEnhet() {
		stubAzure();
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

	//@Test
	public void skalOppdatereStatusTilPdlFantIkkeNyAktoerId() {
		stubAzure();
		stubPdl("hentIdenterBolkSomInneholderNotFound.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, "1234567891123"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();
		/*assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());*/
		Arbeidssak arbeidssak = arbeidssakRepository.findSaksBySakIdIn(List.of(123L)).getFirst();
		assertThat(arbeidssak.getArbeidsstatus()).isEqualTo(FEIL_PDL_FANT_IKKE_AKTOERID);
	}

	//@Test
	public void skalOppdatereAktoerIdOgAvslutteSak() {
		stubAzure();
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

	//@Test
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

	//@Test
	public void skalKastePdlFunctionalException() {
		stubAzure();
		stubPdl("validationError.json");
		populerSakRepository();

		assertThatExceptionOfType(PdlFunctionalException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());
	}

	//@Test
	public void skalFeileBehandlingAvArkivsakMedAapenJournalpost() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_AAPEN_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_AAPEN_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_AAPEN_JOURNALPOST);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");
	}

	//@Test
	public void skalFeileBehandlingAvArkivsakUtenJournalfoerendeEnhet() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET);
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");
	}

	//@Test
	public void skalAvbryteSakerForTomArkivsak() {
		stubAzure();
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

	void populerSakRepository() {
		arbeidssakRepository.saveAll(List.of(
				lagSakForAktoer(123L, "1234567891123"),
				lagSakForAktoer(234L, "1234567891234"),
				lagSakForOrganisasjon(999L, "123456789"))
		);

		commitAndBeginNewTransaction();
	}

	private Arbeidssak lagSakForAktoer(Long sakId, String aktoerId, String fagsakNr) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(fagsakNr)
				.aktoerId(aktoerId)
				.orgnr(null)
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
		return lagSakForAktoer(sakId, aktoerId, null);
	}

	private Arbeidssak lagSakForOrganisasjon(Long sakId, String orgnr) {
		return lagSakForOrganisasjon(sakId, orgnr, null);
	}

}