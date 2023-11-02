package no.nav.dokarkivavlevering.avlevering.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static no.nav.dokarkivavlevering.avlevering.repository.SqlQueries.FINN_FAGOMRADE;
import static no.nav.dokarkivavlevering.avlevering.repository.SqlQueries.FINN_SAKER_SQL;
import static no.nav.dokarkivavlevering.avlevering.repository.SqlQueries.FINN_SAKER_UTEN_DOKUMENTER_SQL;
import static no.nav.dokarkivavlevering.avlevering.repository.SqlQueries.FINN_SAKID_SQL;

@Repository
@Slf4j
public class AvleveringRepository {
	private static final ResultSetExtractor<List<Sak>> SAK_RESULTSET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("id",
					"bruker_id",
					"fagomrade_fagomrade",
					"jp_id",
					"jp_dok_id",
					"jp_dok_fd_id",
					"jp_ae_id",
					"jp_dok_ae_id")
			.newResultSetExtractor(Sak.class);

	private static final ResultSetExtractor<List<Fagomrade>> FAGOMRADE_RESULTSET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("faromrade_fagomrade")
			.newResultSetExtractor(Fagomrade.class);


	private static final ResultSetExtractor<List<Long>> SAKID_RESULTSET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("sakId")
			.newResultSetExtractor(Long.class);

	public static final int ORACLE_MAX_IN = 1000;

	private final AvleveringProperties avleveringProperties;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AvleveringRepository(AvleveringProperties avleveringProperties,
								NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.avleveringProperties = avleveringProperties;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}


	public List<Long> findSakIds(@Body final Tema tema){
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("tema", tema.getTemakode());
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));

		return namedParameterJdbcTemplate.query(FINN_SAKID_SQL, paramMap, SAKID_RESULTSET_EXTRACTOR);

	}

	public Fagomrade getFagomradeForTema(@Body Tema tema) {
		return namedParameterJdbcTemplate.query(FINN_FAGOMRADE, Map.of("tema", tema.getTemakode()), FAGOMRADE_RESULTSET_EXTRACTOR)
				.stream().findFirst().orElseThrow();
	}

	public List<Sak> findSakerUtenDokumenter(final List<Long> sakIds) {
		return doFindSaker(sakIds, false);
	}

	public List<Sak> findSakerMedDokumenter(final List<Long> sakIds) {
		return doFindSaker(sakIds, true);
	}


	private List<Sak> doFindSaker(final List<Long> sakIds, boolean hentDokumenter) {
		if (sakIds.isEmpty()) {
			return emptyList();
		} else if (sakIds.size() > ORACLE_MAX_IN) {
			throw new UnsupportedOperationException("Støtter ikke å hente flere enn " + ORACLE_MAX_IN + " saker om gangen.");
		}

		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("sakIds", sakIds.stream().map(Object::toString).toList());
		paramMap.put("startdato", Timestamp.valueOf(avleveringProperties.getPeriode().getStartdato().atStartOfDay()));
		paramMap.put("sluttdato", Timestamp.valueOf(avleveringProperties.getPeriode().getSluttdato().atStartOfDay()));
		if (hentDokumenter) {
			return namedParameterJdbcTemplate.query(FINN_SAKER_SQL, paramMap, SAK_RESULTSET_EXTRACTOR);
		} else {
			return namedParameterJdbcTemplate.query(FINN_SAKER_UTEN_DOKUMENTER_SQL, paramMap, SAK_RESULTSET_EXTRACTOR);

		}
	}
}
