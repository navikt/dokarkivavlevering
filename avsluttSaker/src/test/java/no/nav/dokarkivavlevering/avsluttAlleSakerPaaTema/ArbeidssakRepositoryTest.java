package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.ENDELIGE_STATUSER;
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
}