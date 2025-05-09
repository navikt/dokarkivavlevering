package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.ENDELIGE_STATUSER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.HENTET_FRA_PDL;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidssakRepositoryTest extends AbstractRepositoryTest {

	//TODO: Legg til tester for de nye metodene i ArbeidssakRepository

	@Test
	void skalHenteSaksIder() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(123L).build(),
				baseArkivsakForAktoerId().sakId(234L).build(),
				baseArkivsakForAktoerId().sakId(345L).build()));
		List<Long> sakIds = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen(ENDELIGE_STATUSER);

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}

	@Test
	void skalKunHenteSakIdHvorSakStatusErNullEllerAapenOgArbeidsstatusIkkeErEndelig() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(1L).status("AAPEN").build(),
				baseArkivsakForAktoerId().sakId(2L).build(),
				baseArkivsakForAktoerId().sakId(3L).status("BAD_STATUS").build(),
				baseArkivsakForAktoerId().sakId(4L).arbeidsstatus(HENTET_FRA_PDL).build(),
				baseArkivsakForAktoerId().sakId(5L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build()
		));

		List<Long> sakIds = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen(ENDELIGE_STATUSER);

		assertThat(sakIds).isEqualTo(List.of(1L, 2L, 4L));
	}

	@Test
	void skalTelleArbeidssaksstatuser() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(123L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForAktoerId().sakId(124L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForAktoerId().sakId(126L).arbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID).build(),
				baseArkivsakForAktoerId().sakId(127L).arbeidsstatus(FEIL_AAPEN_JOURNALPOST).build(),
				baseArkivsakForAktoerId().sakId(128L).arbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID).build(),
				baseArkivsakForAktoerId().sakId(129L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build())
		);

		List<Object[]> antallForHverStatus = arbeidssakRepository.tellAntallArbeidssakerForHverArbeidsstatus();

		assertThat(antallForHverStatus.get(0)[0]).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(antallForHverStatus.get(0)[1]).isEqualTo(3L);
		assertThat(antallForHverStatus.get(1)[0]).isEqualTo(FEIL_PDL_FANT_IKKE_AKTOERID);
		assertThat(antallForHverStatus.get(1)[1]).isEqualTo(2L);
		assertThat(antallForHverStatus.get(2)[0]).isEqualTo(FEIL_AAPEN_JOURNALPOST);
		assertThat(antallForHverStatus.get(2)[1]).isEqualTo(1L);
	}
}