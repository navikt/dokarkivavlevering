package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus.HENTET_FRA_PDL;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidssakRepositoryTest extends AbstractRepositoryTest {

	@Test
	void skalHenteRelevanteSaksIder() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(1L).status("AAPEN").build(),
				baseArkivsakForAktoerId().sakId(2L).build(),
				baseArkivsakForAktoerId().sakId(3L).status("BAD_STATUS").build(),
				baseArkivsakForAktoerId().sakId(4L).arbeidsstatus(HENTET_FRA_PDL).build(),
				baseArkivsakForAktoerId().sakId(5L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForOrganisasjon().sakId(6L).build()
		));

		List<Long> sakIds = arbeidssakRepository.hentAlleUbehandledeSakerMedAktoerId();

		assertThat(sakIds).isEqualTo(List.of(1L, 2L));
	}

	@Test
	@Disabled
	void skalFinneDistinkteAktoerIder() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(1L).aktoerId("111").build(),
				baseArkivsakForAktoerId().sakId(2L).aktoerId("222").arbeidsstatus(HENTET_FRA_PDL).build(),
				baseArkivsakForAktoerId().sakId(3L).aktoerId("222").arbeidsstatus(HENTET_FRA_PDL).build(),
				baseArkivsakForAktoerId().sakId(4L).aktoerId("444").arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForAktoerId().sakId(6L).aktoerId("555").arbeidsstatus(HENTET_FRA_PDL).build(),
				baseArkivsakForAktoerId().sakId(7L).aktoerId("666").arbeidsstatus(FEIL_AAPEN_JOURNALPOST).build(),
				baseArkivsakForAktoerId().sakId(8L).aktoerId(null).build(),
				baseArkivsakForOrganisasjon().sakId(9L).build()
		));

		List<String> distinkteAktoerIder = arbeidssakRepository.hentOppdaterteAktoerIder();

		assertThat(distinkteAktoerIder)
				.hasSize(2)
				.containsAll(List.of("222",  "555"));
	}

	@Test
	@Disabled
	void skalFinneDistinkteOrgnr() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForOrganisasjon().sakId(1L).orgnr("1111").build(),
				baseArkivsakForOrganisasjon().sakId(2L).orgnr("2222").build(),
				baseArkivsakForOrganisasjon().sakId(3L).orgnr("2222").build(),
				baseArkivsakForOrganisasjon().sakId(4L).orgnr("4444").arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForOrganisasjon().sakId(7L).orgnr("6666").arbeidsstatus(FEIL_AAPEN_JOURNALPOST).build(),
				baseArkivsakForOrganisasjon().sakId(8L).orgnr(null).build(),
				baseArkivsakForAktoerId().sakId(9L).build()
		));

		List<String> distinkteOrgnr = arbeidssakRepository.hentOrgnrs();

		assertThat(distinkteOrgnr)
				.hasSize(2)
				.containsAll(List.of("1111", "2222"));
	}

	@Test
	void skalTelleArbeidssaksstatuser() {
		arbeidssakRepository.saveAll(List.of(
				baseArkivsakForAktoerId().sakId(123L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForAktoerId().sakId(124L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build(),
				baseArkivsakForAktoerId().sakId(126L).arbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID).build(),
				baseArkivsakForAktoerId().sakId(127L).arbeidsstatus(FEIL_AAPEN_JOURNALPOST).build(),
				baseArkivsakForAktoerId().sakId(128L).arbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID).build(),
				baseArkivsakForAktoerId().sakId(129L).arbeidsstatus(FERDIG_SAK_AVSLUTTET).build())
		);

		List<Object[]> antallForHverStatus = arbeidssakRepository.tellAntallArbeidssakerForHverArbeidsstatus();

		assertThat(antallForHverStatus.get(0)[0]).isEqualTo(FERDIG_SAK_AVSLUTTET);
		assertThat(antallForHverStatus.get(0)[1]).isEqualTo(3L);
		assertThat(antallForHverStatus.get(1)[0]).isEqualTo(FEIL_PDL_FANT_IKKE_AKTOERID);
		assertThat(antallForHverStatus.get(1)[1]).isEqualTo(2L);
		assertThat(antallForHverStatus.get(2)[0]).isEqualTo(FEIL_AAPEN_JOURNALPOST);
		assertThat(antallForHverStatus.get(2)[1]).isEqualTo(1L);
	}
}