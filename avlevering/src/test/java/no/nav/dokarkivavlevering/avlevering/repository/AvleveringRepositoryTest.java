package no.nav.dokarkivavlevering.avlevering.repository;

import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.config.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationTestConfig.class)
@ActiveProfiles(profiles = {"genererAvlevering", "itest"})
class AvleveringRepositoryTest {

	@Autowired
	private AvleveringRepository avleveringRepository;

	@Test
	void skalKunFinneSakIderMedRiktigKassasjonsstatusOgTillattKilde() {
		List<Long> sakIds = avleveringRepository.findSakIds(Tema.MED);

		assertThat(sakIds)
				.contains(101L)
				.doesNotContain(102L, 103L);
	}

	@Test
	void skalFinneSakSelvOmDatoOpprettetErUtenforPerioden() {
		// Datofilteret er fjernet fra FINN_SAKID_SQL - gammel kode ville forkastet denne
		List<Long> sakIds = avleveringRepository.findSakIds(Tema.MED);

		assertThat(sakIds).contains(104L);
	}

	@Test
	void skalEkskludereAlleKjenteOndemandKilder() {
		List<Long> sakIds = avleveringRepository.findSakIds(Tema.MED);

		assertThat(sakIds).doesNotContain(103L, 111L, 112L, 113L);
	}

	@Test
	void skalFinneSakerMedAlleGyldigeKassasjonsstatuser() {
		List<Long> sakIds = avleveringRepository.findSakIds(Tema.MED);

		assertThat(sakIds).contains(101L, 121L, 122L);
	}

	@Test
	void skalEkskludereJournalposterOpprettetFraOndemandKildeFraBeggeSakSpoerringer() {
		List<Sak> medDokumenter = avleveringRepository.findSakerMedDokumenter(List.of(202L));
		List<Sak> utenDokumenter = avleveringRepository.findSakerUtenDokumenter(List.of(202L));

		assertThat(medDokumenter).isEmpty();
		assertThat(utenDokumenter).isEmpty();
	}

	@Test
	void skalFinneSakSomOppfyllerAlleFiltre() {
		List<Sak> medDokumenter = avleveringRepository.findSakerMedDokumenter(List.of(203L));
		List<Sak> utenDokumenter = avleveringRepository.findSakerUtenDokumenter(List.of(203L));

		assertThat(medDokumenter).extracting(Sak::getId).containsExactly(203L);
		assertThat(utenDokumenter).extracting(Sak::getId).containsExactly(203L);
	}

	@Test
	void skalFinneSakSelvOmDatoOpprettetErUtenforPeriodenForBeggeDokumentSpoerringer() {
		// Datofilteret er fjernet fra både FINN_SAKER_SQL og FINN_SAKER_UTEN_DOKUMENTER_SQL -
		// gammel kode ville forkastet denne saken i begge spørringer
		List<Sak> medDokumenter = avleveringRepository.findSakerMedDokumenter(List.of(204L));
		List<Sak> utenDokumenter = avleveringRepository.findSakerUtenDokumenter(List.of(204L));

		assertThat(medDokumenter).extracting(Sak::getId).containsExactly(204L);
		assertThat(utenDokumenter).extracting(Sak::getId).containsExactly(204L);
	}

	@Test
	void skalKunHenteJournalpostMedTillattKildeNaarSakenHarToJournalposter() {
		List<Sak> medDokumenter = avleveringRepository.findSakerMedDokumenter(List.of(205L));
		List<Sak> utenDokumenter = avleveringRepository.findSakerUtenDokumenter(List.of(205L));

		assertThat(medDokumenter).hasSize(1);
		assertThat(medDokumenter.get(0).getJp()).extracting(Journalpost::getId).containsExactly(2005L);

		assertThat(utenDokumenter).hasSize(1);
		assertThat(utenDokumenter.get(0).getJp()).extracting(Journalpost::getId).containsExactly(2005L);
	}

}
