package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import no.nav.dokarkivavlevering.config.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
		classes = ApplicationTestConfig.class,
		webEnvironment = RANDOM_PORT)
@ActiveProfiles(profiles = {"avsluttSaker", "itest"})
class AvsluttSakRepositoryTest {

	private static final LocalDateTime OPPRETTETDATO_JP_123 = LocalDateTime.parse("2025-01-01T13:30:00");
	private static final LocalDateTime JOURNALDATO_JP_123 = LocalDateTime.parse("2025-01-02T13:30:00");

	private static final LocalDateTime OPPRETTETDATO_JP_234 = LocalDateTime.parse("2025-02-13T14:45:00");
	private static final LocalDateTime JOURNALDATO_JP_234 = LocalDateTime.parse("2025-02-13T15:00:00");

	@Autowired
	protected AvsluttSakRepository avsluttSakRepository;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	public void skalHenteJournalposterForArkivsak() {
		List<Journalpost> journalposter = avsluttSakRepository.getJournalposterForArkivsak(List.of(123L, 234L));

		assertThat(journalposter.get(0))
				.extracting(Journalpost::getOpprettetdato, Journalpost::getJournaldato, Journalpost::getJournalstatus, Journalpost::getJournalfoerendeEnhet, Journalpost::isErFeilregistrert)
				.containsExactly(OPPRETTETDATO_JP_123, JOURNALDATO_JP_123, "FL", "1234", false);
		assertThat(journalposter.get(1))
				.extracting(Journalpost::getOpprettetdato, Journalpost::getJournaldato, Journalpost::getJournalstatus, Journalpost::getJournalfoerendeEnhet, Journalpost::isErFeilregistrert)
				.containsExactly(OPPRETTETDATO_JP_123, JOURNALDATO_JP_123, "E", "1234", false);
		assertThat(journalposter.get(2))
				.extracting(Journalpost::getOpprettetdato, Journalpost::getJournaldato, Journalpost::getJournalstatus, Journalpost::getJournalfoerendeEnhet, Journalpost::isErFeilregistrert)
				.containsExactly(OPPRETTETDATO_JP_234, JOURNALDATO_JP_234, "FS", "5678", false);
	}

	@Test
	void skalOppdatereSakerForArkivsak() {
		List<Long> sakIds = List.of(123L, 234L);
		final SqlParameterSource params = new MapSqlParameterSource()
				.addValue("sakIds", sakIds);

		avsluttSakRepository.updateSakForArkivsak(sakIds);

		List<Sak> saker = namedParameterJdbcTemplate.query("select * from sak where id in (:sakIds);", params, new SakRowMapper());

		assertThat(saker.get(0))
				.extracting(Sak::saksstatus, Sak::avleveringsstatus, Sak::kassasjonsstatus, Sak::endretAv)
				.containsExactly("AVBRUTT", "AVBRUTT", "KLAR_FOR_KASSASJON", "REFERANSE");
		assertThat(saker.get(0).datoEndret).isBefore(LocalDateTime.now());

		assertThat(saker.get(1))
				.extracting(Sak::saksstatus, Sak::avleveringsstatus, Sak::kassasjonsstatus, Sak::endretAv)
				.containsExactly("AVBRUTT", "AVBRUTT", "KLAR_FOR_KASSASJON", "REFERANSE");
		assertThat(saker.get(1).datoEndret).isBefore(LocalDateTime.now());
	}


	public static class SakRowMapper implements RowMapper<Sak> {
		@Override
		public Sak mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Sak(
					rs.getLong("ID"),
					rs.getString("K_SAK_STATUS"),
					rs.getString("K_AVLEVERING_STATUS"),
					rs.getString("K_KASSASJON_STATUS"),
					rs.getString("ENDRET_AV"),
					rs.getTimestamp("DATO_ENDRET") != null ? rs.getTimestamp("DATO_ENDRET").toLocalDateTime() : null,
					rs.getTimestamp("DATO_AVSLUTTET") != null ? rs.getTimestamp("DATO_AVSLUTTET").toLocalDateTime() : null,
					rs.getString("AVSLUTTET_AV"),
					rs.getString("AVSLUTTET_KILDE_NAVN"),
					rs.getTimestamp("DATO_SAK_OPPRETTET") != null ? rs.getTimestamp("DATO_SAK_OPPRETTET").toLocalDateTime() : null,
					rs.getString("ADMINISTRATIV_ENHET"),
					rs.getString("SAK_ANSVARLIG")
			);
		}
	}

	private record Sak(
		Long id,
		String saksstatus,
		String avleveringsstatus,
		String kassasjonsstatus,
		String endretAv,
		LocalDateTime datoEndret,
		LocalDateTime datoAvsluttet,
		String avsluttetAv,
		String avsluttetKildeNavn,
		LocalDateTime datoSakOpprettet,
		String administrativEnhet,
		String sakAnsvarlig
	) {
	}

}