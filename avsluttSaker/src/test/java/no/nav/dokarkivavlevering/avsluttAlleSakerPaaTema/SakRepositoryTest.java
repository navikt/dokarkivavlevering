package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SakRepositoryTest extends AbstractRepositoryTest {

	@BeforeEach
	void setUp() {
		Sak sakForPerson1 = new Sak().builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		Sak sakForPerson2 = new Sak().builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		Sak sakForOrganisasjon = new Sak().builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();

		sakRepository.saveAll(List.of(sakForPerson1, sakForPerson2, sakForOrganisasjon));
	}

	@Test
	void skalHenteAlleSaksIder() {
		List<Long> sakIds = sakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}
}