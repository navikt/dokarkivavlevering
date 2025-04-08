package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

@DataJpaTest
@ContextConfiguration(classes = RepositoryConfig.class)
@ActiveProfiles(profiles = {"avsluttSaker"})
public abstract class AbstractRepositoryTest {

	protected final String AKTOER_ID = "12345678911";
	protected final String FAGSAKSYSTEM_FS22 = "FS22";
	protected final String FAGSAKSYSTEM_AO01 = "AO01";
	protected final String FAGSAKNR = "fagsakNr";

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Autowired
	protected ArbeidssakRepository arbeidssakRepository;

	@AfterEach
	protected void tearDown(){
		arbeidssakRepository.deleteAll();
	}

	protected Arbeidssak.ArbeidssakBuilder baseArkivsak() {
		return Arbeidssak.builder()
				.sakId(123L)
				.applikasjon(FAGSAKSYSTEM_FS22)
				.fagsaknr(null)
				.aktoerId(AKTOER_ID)
				.orgnr(null);
	}

}