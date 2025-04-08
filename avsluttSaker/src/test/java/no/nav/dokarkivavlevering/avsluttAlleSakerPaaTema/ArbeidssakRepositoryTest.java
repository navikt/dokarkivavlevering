package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidssakRepositoryTest extends AbstractRepositoryTest {

	@Test
	void skalHenteSaksIder() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsak().sakId(123L).build(),
				baseArkivsak().sakId(234L).build(),
				baseArkivsak().sakId(345L).build()));
		List<Long> sakIds = arbeidssakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}

	@Test
	void skalKunHenteSakIdHvorSakStatusErNullEllerAapen() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsak().sakId(1L).status("AAPEN").build(),
				baseArkivsak().sakId(2L).build(),
				baseArkivsak().sakId(3L).status("BAD_STATUS").build()));

		List<Long> sakIds = arbeidssakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(1L, 2L));
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErNull() {
		arbeidssakRepository.save(baseArkivsak().fagsaknr(null).build());

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerIdWhereFagsaknrIsNull(AKTOER_ID, FAGSAKSYSTEM_FS22);

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(123L);
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErSatt() {
		arbeidssakRepository.save(baseArkivsak().applikasjon(FAGSAKSYSTEM_AO01).sakId(234L).fagsaknr(FAGSAKNR).build());

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerId(AKTOER_ID, FAGSAKNR, FAGSAKSYSTEM_AO01);

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(234L);
	}

	@Test
	void skalLageArkivsakMedToSaker() {

		arbeidssakRepository.saveAll(List.of(
				baseArkivsak().sakId(123L).applikasjon(FAGSAKSYSTEM_AO01).fagsaknr(FAGSAKNR).build(),
				baseArkivsak().sakId(234L).applikasjon(FAGSAKSYSTEM_AO01).fagsaknr(FAGSAKNR).build()));

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerId(AKTOER_ID, FAGSAKNR, FAGSAKSYSTEM_AO01);

		assertThat(arkivsak)
				.hasSize(2)
				.extracting(Arbeidssak::getSakId).containsAll(List.of(123L, 234L));
	}

}