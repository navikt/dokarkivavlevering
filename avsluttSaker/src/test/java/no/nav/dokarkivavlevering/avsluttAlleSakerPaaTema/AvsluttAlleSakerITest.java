package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.config.AbstractITest;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_ADMINISTRATIV_ENHET_MANGLER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PDL_FANT_IKKE_NY_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PROSESSERING_AV_ARKIVSAK_STARTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.SKAL_IKKE_HENTE_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.Sak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.SakRowMapper;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.assertAvbrutteSaker;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.generateSakParams;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AvsluttAlleSakerITest extends AbstractITest {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	AvsluttAlleSakerService avsluttAlleSakerService;

	private static final Long SAK_MED_LUKKET_JOURNALPOST1 = 123L;
	private static final Long SAK_MED_LUKKET_JOURNALPOST2 = 234L;
	private static final Long SAK_MED_AAPEN_JOURNALPOST = 345L;
	private static final Long SAK_UTEN_FERDIGSTILT_JOURNALPOST = 456L;
	private static final Long SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST = 567L;

	@Test
	public void skalAvslutteSaker() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		stubDvh("response.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST1, "2345678901234"));
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_LUKKET_JOURNALPOST2, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Arbeidssak arbeidssak1 = saker.get(0);
		Arbeidssak arbeidssak2 = saker.get(1);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(PROSESSERING_AV_ARKIVSAK_STARTET.name());
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("2345678901234");
		assertThat(arbeidssak2.getArbeidsstatus()).isEqualTo(PROSESSERING_AV_ARKIVSAK_STARTET.name());
		assertThat(arbeidssak2.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalOppdatereStatusTilPdlFantIkkeNyAktoerId() {
		stubAzure();
		stubPdl("hentIdenterBolkSomInneholderNotFound.json");
		populerSakRepository();

		avsluttAlleSakerService.avsluttAlleSaker();

		Arbeidssak arbeidssak = arbeidssakRepository.findSaksBySakIdIn(List.of(123L)).getFirst();
		assertThat(arbeidssak.getArbeidsstatus()).isEqualTo(PDL_FANT_IKKE_NY_AKTOERID.name());
	}

	@Test
	public void skalOppdatereStatusTilSkalIkkeHenteFraPdl() {
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
		assertThat(arbeidssak.getArbeidsstatus()).isEqualTo(SKAL_IKKE_HENTE_FRA_PDL.name());
	}

	@Test
	public void skalKastePdlFunctionalException() {
		stubAzure();
		stubPdl("validationError.json");
		populerSakRepository();

		assertThatExceptionOfType(PdlFunctionalException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());
	}

	@Test
	public void skalFeileBehandlingAvArkivsakMedAapenJournalpost() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_MED_AAPEN_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_MED_AAPEN_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_AAPEN_JOURNALPOST.name());
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalFeileBehandlingAvArkivsakUtenAdministrativEnhet() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> saker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_JOURNALFOERENDE_ENHET_JOURNALPOST));
		Arbeidssak arbeidssak1 = saker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FEIL_ADMINISTRATIV_ENHET_MANGLER.name());
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");

	}

	@Test
	public void skalAvbryteSakerForTomArkivsak() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		arbeidssakRepository.save(lagSakForAktoer(SAK_UTEN_FERDIGSTILT_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_FERDIGSTILT_JOURNALPOST));
		Arbeidssak arbeidssak1 = arbeidssaker.get(0);

		assertThat(arbeidssak1.getArbeidsstatus()).isEqualTo(FERDIG_TOM_ARKIVSAK.name());
		assertThat(arbeidssak1.getAktoerId()).isEqualTo("1234567891234");

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from sak where id in (:sakIds);", generateSakParams(SAK_UTEN_FERDIGSTILT_JOURNALPOST), new SakRowMapper());
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

	private Arbeidssak lagSakForAktoer(Long sakId, String aktoerId) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();
	}

	private Arbeidssak lagSakForOrganisasjon(Long sakId, String orgnr) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr(orgnr)
				.build();
	}

}