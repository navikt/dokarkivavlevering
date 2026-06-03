package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SqlQueries.HENT_NAVN_FOR_ADMINISTRATIV_ENHET;

@Repository
@Profile("avsluttSaker")
public class AdministrativEnhetJdbcRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AdministrativEnhetJdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public String hentNavnForAdministrativEnhet(String fagomraade, LocalDate opprettetTidspunkt) {
		if (fagomraade == null || opprettetTidspunkt == null) {
			return null;
		}

		SqlParameterSource params = new MapSqlParameterSource(Map.of(
				"fagomraade", fagomraade,
				"opprettet_tidspunkt", opprettetTidspunkt)
		);

		try {
			return namedParameterJdbcTemplate.queryForObject(HENT_NAVN_FOR_ADMINISTRATIV_ENHET, params, String.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}
