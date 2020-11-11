package no.nav.dokarkivavlevering.avlevering.repository;

import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.apache.camel.Body;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class AvleveringRepository {

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
		return namedParameterJdbcTemplate.queryForList("select sa.id " +
						"from t_journalpost j " +
						"join t_saksrelasjon s on j.journalpost_id = s.journalpost_id " +
						"join sak sa on sa.id = to_number(regexp_replace(s.sak_nr_fk,'[^0-9]','')) " +
						"where sa.tema = :tema " +
						"and (s.feilregistrert is null or s.feilregistrert = 0) " +
						"and j.k_journal_s in('J','FS','FL','E') " +
						"and j.dato_opprettet >= :startdato " +
						"and j.dato_opprettet <= :sluttdato " +
						"order by sa.ID",
				paramMap, Long.class);
	}
}
