package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.config.AbstractITest;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AvsluttAlleSakerITest extends AbstractITest {

	@Autowired
	AvsluttAlleSakerService avsluttAlleSakerService;

	@BeforeEach
	public void setUp() {
		populerSakRepository();
	}

	@Test
	public void skalAvslutteSaker() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Sak> sak1 = sakRepository.findSaksBySakIdIn(List.of(123L));
		assertThat(sak1.getFirst().getStatus()).isEqualTo("HENTET_FRA_PDL");
	}

	@Test
	public void skalKastePdlFunctionalException() {
		stubAzure();
		stubPdl("validationError.json");

		assertThatExceptionOfType(PdlFunctionalException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());
	}


	void populerSakRepository() {
		Sak sakForPerson1 = Sak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		Sak sakForPerson2 = Sak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		Sak sakForOrganisasjon = Sak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();

		sakRepository.saveAll(List.of(sakForPerson1, sakForPerson2, sakForOrganisasjon));
		commitAndBeginNewTransaction();
	}

}