package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakRepositoryTest.ADMINISTRATIV_ENHET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakRepositoryTest.OPPRETTETDATO_JP_123;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

public class SakRepositoryUtils {

	public static SqlParameterSource generateSakParams(Long sakId) {
		return generateSakParams(List.of(sakId));
	}

	public static SqlParameterSource generateSakParams(List<Long> sakIder) {
		return new MapSqlParameterSource()
				.addValue("sakIds", sakIder);
	}

	public static void assertAvbrutteSaker(List<Sak> saker){
		assertThat(saker)
				.extracting(Sak::saksstatus, Sak::avleveringsstatus, Sak::kassasjonsstatus, Sak::endretAv)
				.containsOnly(
						tuple("AVBRUTT", "AVBRUTT", "KLAR_FOR_KASSASJON", "REFERANSE")
				);

		assertThat(saker)
				.allSatisfy(sak -> assertThat(sak.datoEndret()).isCloseTo(now(), within(10, SECONDS)));
	}

	public static void assertAvsluttedeSaker(List<Sak> saker){
		assertThat(saker)
				.extracting(Sak::saksstatus, Sak::avleveringsstatus, Sak::kassasjonsstatus, Sak::endretAv, Sak::endretKildeNavn, Sak::avsluttetAv, Sak::avsluttetKildeNavn, Sak::administrativEnhet, Sak::sakAnsvarlig)
				.containsOnly(
						tuple("AVSLUTTET", null, null, "REFERANSE", "AvsluttSakerPaaTema", "JOARK", "JOARK", ADMINISTRATIV_ENHET, ADMINISTRATIV_ENHET)
				);

		assertThat(saker)
				.allSatisfy(sak -> {
					assertThat(sak.datoEndret()).isCloseTo(now(), within(10, SECONDS));
					assertThat(sak.datoAvsluttet()).isCloseTo(now(), within(10, SECONDS));
					assertThat(sak.datoSakOpprettet()).isEqualTo(OPPRETTETDATO_JP_123);
				});
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
					rs.getString("ENDRET_KILDE_NAVN"),
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

	public record Sak(
			Long id,
			String saksstatus,
			String avleveringsstatus,
			String kassasjonsstatus,
			String endretAv,
			String endretKildeNavn,
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
