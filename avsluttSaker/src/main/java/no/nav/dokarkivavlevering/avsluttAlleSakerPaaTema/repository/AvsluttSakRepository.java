package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Journalpost;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SqlQueries.AVBRYT_SAKER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SqlQueries.AVSLUTT_SAKER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SqlQueries.FINN_JOURNALPOSTER_FOR_ARKIVSAK;

@Repository
@Profile("avsluttSaker")
public class AvsluttSakRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final AvsluttSakProperties avsluttSakProperties;

	public AvsluttSakRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, AvsluttSakProperties avsluttSakProperties) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.avsluttSakProperties = avsluttSakProperties;
	}

	public List<Journalpost> getJournalposterForArkivsak(List<Long> sakIds) {
		final SqlParameterSource params = new MapSqlParameterSource()
				.addValue("sakIds", sakIds);

		return namedParameterJdbcTemplate.query(FINN_JOURNALPOSTER_FOR_ARKIVSAK, params, new JournalpostRowMapper());
	}

	public void avbrytSaker(List<Long> saksIder) {
		final SqlParameterSource params = new MapSqlParameterSource()
				.addValue("referanse", avsluttSakProperties.getReferanse())
				.addValue("sakIds", saksIder);

		namedParameterJdbcTemplate.update(AVBRYT_SAKER, params);
	}

	public void avsluttSaker(List<Long> saksIder, LocalDateTime datoAvsluttet, LocalDateTime datoSakOpprettet, String administrativEnhet) {
		final SqlParameterSource params = new MapSqlParameterSource()
				.addValue("referanse", avsluttSakProperties.getReferanse())
				.addValue("sakIds", saksIder)
				.addValue("datoAvsluttet", datoAvsluttet)
				.addValue("datoSakOpprettet", datoSakOpprettet)
				.addValue("administrativEnhet", administrativEnhet)
				.addValue("sakAnsvarlig", administrativEnhet);

		namedParameterJdbcTemplate.update(AVSLUTT_SAKER, params);
	}

	public static class JournalpostRowMapper implements RowMapper<Journalpost> {
		@Override
		public Journalpost mapRow(ResultSet rs, int rowNum) throws SQLException {
			Journalpost journalpost = new Journalpost();

			journalpost.setErFeilregistrert(rs.getBoolean("erFeilregistrert"));
			journalpost.setOpprettetdato(rs.getTimestamp("opprettetdato").toLocalDateTime());
			journalpost.setJournaldato(rs.getTimestamp("journaldato").toLocalDateTime().toLocalDate());
			journalpost.setJournalfoerendeEnhet(rs.getString("journalfoerendeEnhet"));
			journalpost.setJournalstatus(rs.getString("journalstatus"));

			return journalpost;
		}
	}
}
