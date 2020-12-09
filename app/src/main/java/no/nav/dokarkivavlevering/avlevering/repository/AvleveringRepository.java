package no.nav.dokarkivavlevering.avlevering.repository;

import no.nav.dokarkivavlevering.avlevering.AvleveringTemaRoute;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.IdRange;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Header;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
					"jp_id",
					"jp_dok_id",
					"jp_dok_fd_id",
					"jp_ae_id",
					"jp_dok_ae_id")
			.newResultSetExtractor(Sak.class);
	public static final int ORACLE_MAX_IN = 1000;

	private final AvleveringProperties avleveringProperties;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AvleveringRepository(AvleveringProperties avleveringProperties,
								NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.avleveringProperties = avleveringProperties;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	// Keyset pagination
	public List<Long> findSakIdsPagination(@Body final Tema tema, @Header(AvleveringTemaRoute.HEADER_LAST_SAK_ID) final Long lastSakId,
										   @ExchangeProperty(AvleveringTemaRoute.PROPERTY_TEMA_IDRANGE) final IdRange idRange) {
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("batchsize", avleveringProperties.getBatchsize());
		paramMap.put("lastSakId", lastSakId);
		paramMap.put("tema", tema.getTemakode());
		paramMap.put("minJournalpostId", idRange.getJournalpostIdMin());
		paramMap.put("maxJournalpostId", idRange.getJournalpostIdMax());
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.queryForList(SqlQueries.FINN_SAK_PAGE, paramMap, Long.class);
	}

	public List<Sak> findSaker(final List<Long> sakIds) {
		if (sakIds.isEmpty()) {
			return new ArrayList<>();
		} else if (sakIds.size() > ORACLE_MAX_IN) {
			throw new UnsupportedOperationException("Støtter ikke å hente flere enn " + ORACLE_MAX_IN + " saker om gangen.");
		}

		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("sakIds", sakIds.stream().map(Object::toString).collect(Collectors.toList()));
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.query(SqlQueries.FINN_SAKER_SQL, paramMap, SAK_RESULTSET_EXTRACTOR);
	}

	public IdRange findJournalpostIdRange(@Body final Tema tema) {
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("tema", tema.getTemakode());
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		return namedParameterJdbcTemplate.queryForObject(SqlQueries.JOURNALPOST_ID_RANGE, paramMap,
				(rs, rowNum) -> new IdRange(rs.getLong(1), rs.getLong(2), rs.getLong(3)));
	}
}
