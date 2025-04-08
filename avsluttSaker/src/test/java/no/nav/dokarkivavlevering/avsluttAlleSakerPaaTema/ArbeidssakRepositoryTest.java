package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidssakRepositoryTest extends AbstractRepositoryTest {

	@Test
	void skalHenteSaksIder() {
		genererTestSaker();
		List<Long> sakIds = arbeidssakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));
	}

	@Test
	void skalKunHenteSakIdHvorSakStatusErNullEllerAapen() {
		arbeidssakRepository.save(genererSakBuilder().sakId(1L).status("AAPEN").build());
		arbeidssakRepository.save(genererSakBuilder().sakId(2L).build());
		arbeidssakRepository.save(genererSakBuilder().sakId(3L).status("BAD_STATUS").build());

		List<Long> sakIds = arbeidssakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(1L,2L));
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErNull() {
		Arbeidssak arbeidssakForPerson1 = Arbeidssak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		arbeidssakRepository.save(arbeidssakForPerson1);

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerIdWhereFagsaknrIsNull("12345678911", "FS22");

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(123L);
	}

	@Test
	void skalLageArkivsakMedÉnSakDerFagsaknrErSatt() {
		Arbeidssak arenasak = Arbeidssak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		arbeidssakRepository.save(arenasak);

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerId("12345678912", "123", "AO01");

		assertThat(arkivsak).hasSize(1);
		assertThat(arkivsak.get(0).getSakId()).isEqualTo(234L);
	}

	@Test
	void skalLageArkivsakMedToSaker() {
		var aktoerId = "12345678911";

		Arbeidssak arbeidssak1 = Arbeidssak.builder()
				.sakId(123L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();

		Arbeidssak arbeidssak2 = Arbeidssak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId(aktoerId)
				.orgnr(null)
				.build();

		arbeidssakRepository.saveAll(List.of(arbeidssak1, arbeidssak2));

		List<Arbeidssak> arkivsak = arbeidssakRepository.findArkivsakForAktoerId(aktoerId, "123", "AO01");

		assertThat(arkivsak)
				.hasSize(2)
				.extracting(Arbeidssak::getSakId).containsAll(List.of(123L, 234L));
	}

	void genererTestSaker() {
		Arbeidssak arbeidssakForPerson1 = Arbeidssak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		Arbeidssak arbeidssakForPerson2 = Arbeidssak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		Arbeidssak arbeidssakForOrganisasjon = Arbeidssak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();

		arbeidssakRepository.saveAll(List.of(arbeidssakForPerson1, arbeidssakForPerson2, arbeidssakForOrganisasjon));
	}

	private Arbeidssak.ArbeidssakBuilder genererSakBuilder(){
		return Arbeidssak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null);
	}
}