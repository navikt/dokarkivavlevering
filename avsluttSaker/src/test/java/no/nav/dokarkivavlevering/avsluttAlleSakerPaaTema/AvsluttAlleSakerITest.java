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
 	}

	@Test
	public void skalAvslutteSaker() {
		stubAzure();
		stubPdl("hentIdenterBolk.json");
		populerSakRepository();

		avsluttAlleSakerService.avsluttAlleSaker();

		List<Sak> saker = sakRepository.findSaksBySakIdIn(List.of(123L, 234L));
		Sak sak1 = saker.get(0);
		Sak sak2 = saker.get(1);

		assertThat(sak1.getArbeidsStatus()).isEqualTo("HENTET_FRA_PDL");
		assertThat(sak1.getAktoerId()).isEqualTo("2345678901234");
		assertThat(sak2.getArbeidsStatus()).isEqualTo("MIDLERTIDIG_STATUS");
		assertThat(sak2.getAktoerId()).isEqualTo("1234567891234");
	}

	@Test
	public void skalOppdatereStatusTilPdlFantIkkeNyAktoerId() {
		stubAzure();
		stubPdl("hentIdenterBolkSomInneholderNotFound.json");
		populerSakRepository();

		avsluttAlleSakerService.avsluttAlleSaker();

		Sak sak = sakRepository.findSaksBySakIdIn(List.of(123L)).getFirst();
		assertThat(sak.getArbeidsStatus()).isEqualTo("PDL_FANT_IKKE_NY_AKTOERID");
	}

	@Test
	public void skalOppdatereStatusTilSkalIkkeHenteFraPdl() {
		Sak sakForOrganisasjon = Sak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();
		sakRepository.save(sakForOrganisasjon);
		commitAndBeginNewTransaction();

		avsluttAlleSakerService.avsluttAlleSaker();

		Sak sak = sakRepository.findSaksBySakIdIn(List.of(345L)).getFirst();
		assertThat(sak.getArbeidsStatus()).isEqualTo("SKAL_IKKE_HENTE_FRA_PDL");
	}

	@Test
	public void skalKastePdlFunctionalException() {
		stubAzure();
		stubPdl("validationError.json");
		populerSakRepository();

		assertThatExceptionOfType(PdlFunctionalException.class)
				.isThrownBy(() -> avsluttAlleSakerService.avsluttAlleSaker());
	}


	void populerSakRepository() {
		Sak sakForPerson1 = Sak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("1234567891123")
				.orgnr(null)
				.build();

		Sak sakForPerson2 = Sak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("1234567891234")
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