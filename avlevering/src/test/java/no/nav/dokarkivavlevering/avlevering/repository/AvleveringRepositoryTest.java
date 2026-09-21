package no.nav.dokarkivavlevering.avlevering.repository;

import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.domain.StartSluttDato;
import no.nav.dokarkivavlevering.config.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationTestConfig.class)
@ActiveProfiles(profiles = {"genererAvlevering", "itest"})
class AvleveringRepositoryTest {

	private static long FEIL_KASSASJONSSTATUS = 102L;
	private static long FEIL_ONDEMAND = 103L;
	private static long FEIL_ONDEMAND2 = 111L;
	private static long FEIL_ONDEMAND3 = 112L;
	private static long FEIL_ONDEMAND4 = 113L;
	private static long FEIL_JOURNALSTATUS = 105L;

	@Autowired
	private AvleveringRepository avleveringRepository;

	@Test
	void skalFinneAlleSakIds() {
		List<Long> sakIds = avleveringRepository.findSakIds(Tema.MED);

		assertThat(sakIds).contains(101L, 121L, 122L);

		assertThat(sakIds).doesNotContain(FEIL_KASSASJONSSTATUS, FEIL_ONDEMAND, FEIL_ONDEMAND2, FEIL_ONDEMAND3, FEIL_ONDEMAND4, FEIL_JOURNALSTATUS);
	}

	@Test
	void skalFinneStartOgSluttdatoForTema() {
		StartSluttDato startSluttDato = avleveringRepository.findStartOgSluttdato("MED");

		assertThat(startSluttDato.startdato()).isEqualTo(LocalDateTime.parse("2019-06-15T08:00:00").toLocalDate());
		assertThat(startSluttDato.sluttdato()).isEqualTo(LocalDateTime.parse("2021-03-10T14:00:00").toLocalDate());
	}

	@Test
	void skalEkskludereJournalpostsFraOndemand() {
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
	void skalKunHenteJournalpostSomOppfyllerAlleFiltreNaarSakenHarFlereJournalposter() {
		// Sak 205 har tre journalposter: 2005 oppfyller alle filtre, 2006 har en ekskludert
		// ondemand-kilde, og 2007 har et dokument som ikke er FERDIGSTILT. Kun 2005 skal hentes ut.
		List<Sak> medDokumenter = avleveringRepository.findSakerMedDokumenter(List.of(205L));
		List<Sak> utenDokumenter = avleveringRepository.findSakerUtenDokumenter(List.of(205L));

		assertThat(medDokumenter).hasSize(1);
		assertThat(medDokumenter.get(0).getJp()).extracting(Journalpost::getId).containsExactly(2005L);

		assertThat(utenDokumenter).hasSize(1);
		assertThat(utenDokumenter.get(0).getJp()).extracting(Journalpost::getId).containsExactly(2005L);
	}
}
