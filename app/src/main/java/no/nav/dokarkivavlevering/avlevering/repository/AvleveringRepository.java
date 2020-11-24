package no.nav.dokarkivavlevering.avlevering.repository;

import no.nav.dokarkivavlevering.avlevering.AvleveringTemaRoute;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.exception.AvleveringFunctionalException;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class AvleveringRepository {
	private static final ResultSetExtractor<List<Sak>> SAK_RESULTSET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("id",
					"bruker_id",
					"journalposter_id",
					"journalposter_dokumenter_id",
					"journalposter_dokumenter_fildetaljer_id",
					"journalposter_arkivendringer_id",
					"journalposter_dokumenter_arkivendringer_id")
			.newResultSetExtractor(Sak.class);
	public static final int ORACLE_MAX_IN = 1000;

	private final AvleveringProperties avleveringProperties;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AvleveringRepository(AvleveringProperties avleveringProperties,
								NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.avleveringProperties = avleveringProperties;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<Long> findSakIdForTema(@Body final String tema) {
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("tema", tema);
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.queryForList("select distinct sa.id " +
						"from t_journalpost j " +
						"join t_saksrelasjon s on j.journalpost_id = s.journalpost_id " +
						"join sak sa on sa.id = to_number(regexp_replace(s.sak_nr_fk,'[^0-9]','')) " +
						"where sa.tema = :tema " +
						"and (s.feilregistrert is null or s.feilregistrert = 0) " +
						"and j.k_journal_s in('J','FS','FL','E') " +
						"and trunc(j.dato_opprettet) >= :startdato " +
						"and trunc(j.dato_opprettet) <= :sluttdato " +
						"order by sa.id",
				paramMap, Long.class);
	}

	// Keyset pagination
	public List<Long> findSakIdsPagination(@Body final String tema, @Header(AvleveringTemaRoute.HEADER_LAST_SAK_ID) final Long lastSakId) {
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("batchsize", avleveringProperties.getPeriode().getBatchsize());
		paramMap.put("lastSakId", lastSakId);
		paramMap.put("tema", tema);
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.queryForList("select distinct sa.id\n" +
						"from t_journalpost j\n" +
						"         join t_saksrelasjon s on j.journalpost_id = s.journalpost_id\n" +
						"         join sak sa on sa.id = to_number(regexp_replace(s.sak_nr_fk, '[^0-9]', ''))\n" +
						"where sa.id < :lastSakId and\n" +
						"      sa.tema = :tema\n" +
						"  and (s.feilregistrert is null or s.feilregistrert = 0)\n" +
						"  and j.k_journal_s in ('J', 'FS', 'FL', 'E')\n" +
						"  and j.dato_opprettet >= :startdato\n" +
						"  and j.dato_opprettet <= :sluttdato\n" +
						"order by sa.id desc\n" +
						"    fetch first :batchsize rows only",
				paramMap, Long.class);
	}

	public List<Sak> findSaker(final List<Long> sakIds) {
		if (sakIds.isEmpty()) {
			return new ArrayList<>();
		}
		if (sakIds.size() > ORACLE_MAX_IN) {
			throw new UnsupportedOperationException("Støtter ikke å hente flere enn " + ORACLE_MAX_IN + " saker om gangen.");
		} else if (sakIds.size() < 1) {
			throw new AvleveringFunctionalException("Fant ingen saker ");
		}

		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("sakIds", sakIds.stream().map(Object::toString).collect(Collectors.toList()));
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.query(SqlQueries.FINN_SAKER_SQL, paramMap, SAK_RESULTSET_EXTRACTOR);
	}

	public InputStream getDokument(final String filUuid) {
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("filUuid", filUuid);
		return namedParameterJdbcTemplate.query("select fil from t_dokument_fil where fil_uuid = :filUuid", paramMap, resultSet -> {
			return resultSet.getBinaryStream(1);
		});
	}
}
