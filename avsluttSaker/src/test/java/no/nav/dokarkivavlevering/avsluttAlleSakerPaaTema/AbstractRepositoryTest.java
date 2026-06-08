package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = RepositoryConfig.class)
@ActiveProfiles(profiles = {"avsluttSaker"})
public abstract class AbstractRepositoryTest {

	protected final String AKTOER_ID = "12345678911";
	protected final String ORGNR = "123456789";
	protected final String FAGSAKSYSTEM_FS22 = "FS22";

	@Autowired
	protected ArbeidssakRepository arbeidssakRepository;

	@AfterEach
	protected void tearDown(){
		arbeidssakRepository.deleteAll();
	}

	protected Arbeidssak.ArbeidssakBuilder baseArkivsakForAktoerId() {
		return Arbeidssak.builder()
				.sakId(123L)
				.applikasjon(FAGSAKSYSTEM_FS22)
				.fagsaknr(null)
				.aktoerId(AKTOER_ID)
				.orgnr(null);
	}

	protected Arbeidssak.ArbeidssakBuilder baseArkivsakForOrganisasjon() {
		return Arbeidssak.builder()
				.sakId(123L)
				.applikasjon(FAGSAKSYSTEM_FS22)
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr(ORGNR);
	}

}