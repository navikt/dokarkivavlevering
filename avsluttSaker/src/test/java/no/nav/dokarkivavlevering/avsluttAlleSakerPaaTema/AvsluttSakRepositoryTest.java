package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import no.nav.dokarkivavlevering.config.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.Sak;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.SakRowMapper;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.assertAvbrutteSaker;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.SakRepositoryUtils.generateSakParams;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
		classes = ApplicationTestConfig.class,
		webEnvironment = RANDOM_PORT)
@ActiveProfiles(profiles = {"avsluttSaker", "itest"})
class AvsluttSakRepositoryTest {

	private static final LocalDateTime OPPRETTETDATO_JP_123 = LocalDateTime.parse("2025-01-01T13:30:00");
	private static final LocalDate JOURNALDATO_JP_123 = LocalDate.parse("2025-01-02");

	private static final LocalDateTime OPPRETTETDATO_JP_234 = LocalDateTime.parse("2025-02-13T14:45:00");
	private static final LocalDate JOURNALDATO_JP_234 = LocalDate.parse("2025-02-13");

	@Autowired
	protected AvsluttSakRepository avsluttSakRepository;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	public void skalHenteJournalposterForArkivsak() {
		List<Journalpost> journalposter = avsluttSakRepository.getJournalposterForArkivsak(List.of(123L, 234L));

		assertThat(journalposter)
				.extracting(Journalpost::getOpprettetdato, Journalpost::getJournaldato, Journalpost::getJournalstatus, Journalpost::getJournalfoerendeEnhet, Journalpost::isErFeilregistrert)
				.containsExactlyInAnyOrder(
						tuple(OPPRETTETDATO_JP_123, JOURNALDATO_JP_123, "FL", "1234", false),
						tuple(OPPRETTETDATO_JP_123, JOURNALDATO_JP_123, "E", "1234", false),
						tuple(OPPRETTETDATO_JP_234, JOURNALDATO_JP_234, "FS", "5678", false)
				);
	}

	@Test
	void skalOppdatereSakerForArkivsak() {
		List<Long> sakIds = List.of(123L, 234L);

		avsluttSakRepository.updateSakForArkivsak(sakIds);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from sak where id in (:sakIds);", generateSakParams(sakIds), new SakRowMapper());

		assertAvbrutteSaker(saker);
	}

}