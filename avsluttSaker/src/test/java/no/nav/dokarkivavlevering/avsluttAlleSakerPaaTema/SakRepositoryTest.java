package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SakRepositoryTest extends AbstractRepositoryTest {

	@Test
	void skalHenteSaksIder() {
		genererTestSaker();
		List<Long> sakIds = sakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}

	@Test
	void skalKunHenteSakIdHvorSakStatusErNullEllerAapen() {
		sakRepository.save(genererSakBuilder().sakId(1L).status("AAPEN").build());
		sakRepository.save(genererSakBuilder().sakId(2L).build());
		sakRepository.save(genererSakBuilder().sakId(3L).status("BAD_STATUS").build());

		List<Long> sakIds = sakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(1L,2L));
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErNull() {
		Sak sakForPerson1 = Sak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		sakRepository.save(sakForPerson1);

		List<Sak> arkivsak = sakRepository.findArkivsakForAktoerIdWhereFagsaknrIsNull("12345678911", "FS22");

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(123L);
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErSatt() {
		Sak arenasak = Sak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		sakRepository.save(arenasak);

		List<Sak> arkivsak = sakRepository.findArkivsakForAktoerId("12345678912", "123", "AO01");

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(234L);
	}

	@Test
	void skalLageArkivsakMedToSaker() {
		var aktoerId = "12345678911";

		Sak sak1 = Sak.builder()
				.sakId(123L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();

		Sak sak2 = Sak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();

		sakRepository.saveAll(List.of(sak1, sak2));

		List<Sak> arkivsak = sakRepository.findArkivsakForAktoerId(aktoerId, "123", "AO01");

		assertThat(arkivsak)
				.hasSize(2)
				.extracting(Sak::getSakId).containsAll(List.of(123L, 234L));
	}

	void genererTestSaker() {
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
	}

	private Sak.SakBuilder genererSakBuilder(){
		return Sak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null);
	}
}