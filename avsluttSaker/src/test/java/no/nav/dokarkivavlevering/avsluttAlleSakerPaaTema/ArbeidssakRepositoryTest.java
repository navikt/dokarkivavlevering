package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidssakRepositoryTest extends AbstractRepositoryTest {

	//TODO: Legg til tester for de nye metodene i ArbeidssakRepository

	@Test
	void skalHenteSaksIder() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(123L).build(),
				baseArkivsakForAktoerId().sakId(234L).build(),
				baseArkivsakForAktoerId().sakId(345L).build()));
		List<Long> sakIds = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}

	@Test
	void skalKunHenteSakIdHvorSakStatusErNullEllerAapen() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(1L).status("AAPEN").build(),
				baseArkivsakForAktoerId().sakId(2L).build(),
				baseArkivsakForAktoerId().sakId(3L).status("BAD_STATUS").build()));

		List<Long> sakIds = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen();

		assertThat(sakIds).isEqualTo(List.of(1L, 2L));
	}
}