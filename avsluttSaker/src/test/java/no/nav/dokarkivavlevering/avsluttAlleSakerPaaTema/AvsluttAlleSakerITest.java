package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.config.AbstractITest;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PDL_FANT_IKKE_NY_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PROSESSERING_AV_ARKIVSAK_STARTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.SKAL_IKKE_HENTE_FRA_PDL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AvsluttAlleSakerITest extends AbstractITest {

	@Autowired
	AvsluttAlleSakerService avsluttAlleSakerService;

	@BeforeEach
	public void setUp() {
 	}

	private static final Long SAK_MED_LUKKET_JOURNALPOST1 = 123L;
	private static final Long SAK_MED_LUKKET_JOURNALPOST2 = 234L;
	private static final Long SAK_MED_AAPEN_JOURNALPOST = 345L;
	private static final Long SAK_UTEN_FERDIGSTILT_JOURNALPOST = 456L;

	@Test
	public void skalAvslutteSaker() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		populerSakRepository();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Sak> saker = sakRepository.findSaksBySakIdIn(List.of(SAK_MED_LUKKET_JOURNALPOST1, SAK_MED_LUKKET_JOURNALPOST2));
		Sak sak1 = saker.get(0);
		Sak sak2 = saker.get(1);

		assertThat(sak1.getArbeidsstatus()).isEqualTo(PROSESSERING_AV_ARKIVSAK_STARTET.name());
		assertThat(sak1.getAktoerId()).isEqualTo("2345678901234");
		assertThat(sak2.getArbeidsstatus()).isEqualTo(PROSESSERING_AV_ARKIVSAK_STARTET.name());
		assertThat(sak2.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalOppdatereStatusTilPdlFantIkkeNyAktoerId() {
		stubAzure();
		stubPdl("hentIdenterBolkSomInneholderNotFound.json");
		populerSakRepository();

		avsluttAlleSakerService.avsluttAlleSaker();

		Sak sak = sakRepository.findSaksBySakIdIn(List.of(123L)).getFirst();
		assertThat(sak.getArbeidsstatus()).isEqualTo(PDL_FANT_IKKE_NY_AKTOERID.name());
	}

	@Test
	public void skalOppdatereStatusTilSkalIkkeHenteFraPdl() {
		Sak sakForOrganisasjon = Sak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();
		sakRepository.save(sakForOrganisasjon);
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		Sak sak = sakRepository.findSaksBySakIdIn(List.of(345L)).getFirst();
		assertThat(sak.getArbeidsstatus()).isEqualTo(SKAL_IKKE_HENTE_FRA_PDL.name());
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
		sakRepository.save(lagSakForAktoer(SAK_MED_AAPEN_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Sak> saker = sakRepository.findSaksBySakIdIn(List.of(SAK_MED_AAPEN_JOURNALPOST));
		Sak sak1 = saker.get(0);

		assertThat(sak1.getArbeidsstatus()).isEqualTo(FEIL_AAPEN_JOURNALPOST.name());
		assertThat(sak1.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalAvbryteSakerForTomArkivsak() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		sakRepository.save(lagSakForAktoer(SAK_UTEN_FERDIGSTILT_JOURNALPOST, "1234567891234"));
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Sak> saker = sakRepository.findSaksBySakIdIn(List.of(SAK_UTEN_FERDIGSTILT_JOURNALPOST));
		Sak sak1 = saker.get(0);

		assertThat(sak1.getArbeidsstatus()).isEqualTo(FERDIG_TOM_ARKIVSAK.name());
		assertThat(sak1.getAktoerId()).isEqualTo("1234567891234");
	}

	void populerSakRepository() {
		sakRepository.saveAll(List.of(
				lagSakForAktoer(123L, "1234567891123"),
				lagSakForAktoer(234L, "1234567891234"),
				lagSakForOrganisasjon(999L, "123456789"))
		);

		commitAndBeginNewTransaction();
	}

	private Sak lagSakForAktoer(Long sakId, String aktoerId) {
		return Sak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();
	}

	private Sak lagSakForOrganisasjon(Long sakId, String orgnr) {
		return Sak.builder()
				.sakId(sakId)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr(orgnr)
				.build();
	}

}