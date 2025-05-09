package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.grupperArbeidssakerPerAktoerId;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils.AvsluttSakUtils.grupperArbeidssakerPerOrgnr;
import static org.assertj.core.api.Assertions.assertThat;

class AvsluttSakUtilsTest {

	private static final String AKTOERID_1 = "12345678901";
	private static final String AKTOERID_2 = "23456789012";
	private static final String AKTOERID_3 = "34567890123";
	private static final String ORGNR_1 = "111222333";
	private static final String ORGNR_2 = "222333444";
	private static final String ORGNR_3 = "333444555";
	private static final String APPLIKASJON_UTEN_FAGSAKNR = "FS22";
	private static final String APPLIKASJON_MED_FAGSAKNR = "AO01";

	@Test
	void skalIkkeGruppereSakerMedAktoerIdUtenFagsaknr() {
		List<Arbeidssak> arbeidssakerUtenFagsaknr = List.of(
			lagArbeidssakMedAktoerId(1L, AKTOERID_1, APPLIKASJON_UTEN_FAGSAKNR, null),
			lagArbeidssakMedAktoerId(2L, AKTOERID_1, APPLIKASJON_UTEN_FAGSAKNR, null),
			lagArbeidssakMedAktoerId(3L, AKTOERID_1, null, null)
		);

		List<List<Arbeidssak>> grupperteArbeidssaker = grupperArbeidssakerPerAktoerId(arbeidssakerUtenFagsaknr);

		assertThat(grupperteArbeidssaker).hasSize(3)
				.extracting(List::size)
				.containsOnly(1);
	}

	@Test
	void skalGruppereSakerMedAktoerId() {
		List<Arbeidssak> arbeidssaker = List.of(
				lagArbeidssakMedAktoerId(1L, AKTOERID_1, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedAktoerId(2L, AKTOERID_2, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedAktoerId(3L, AKTOERID_1, APPLIKASJON_MED_FAGSAKNR, "2222"),
				lagArbeidssakMedAktoerId(4L, AKTOERID_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedAktoerId(5L, AKTOERID_3, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedAktoerId(6L, AKTOERID_2, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedAktoerId(7L, AKTOERID_2, APPLIKASJON_MED_FAGSAKNR, "1234"),
				lagArbeidssakMedAktoerId(8L, AKTOERID_2, APPLIKASJON_MED_FAGSAKNR, "2345"),
				lagArbeidssakMedAktoerId(9L, AKTOERID_2, APPLIKASJON_MED_FAGSAKNR, "1234"),
				lagArbeidssakMedAktoerId(10L, AKTOERID_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedAktoerId(11L, AKTOERID_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedAktoerId(12L, AKTOERID_3, APPLIKASJON_MED_FAGSAKNR, "2345")
		);

		List<List<Arbeidssak>> grupperteArbeidssaker = grupperArbeidssakerPerAktoerId(arbeidssaker);

		assertThat(grupperteArbeidssaker).hasSize(9)
				.extracting(List::size)
				.containsAll(List.of(1, 1, 1, 1, 1, 1, 1, 2, 3));
	}

	@Test
	void skalIkkeGruppereSakerMedOrgnrUtenFagsaknr() {
		List<Arbeidssak> arbeidssakerUtenFagsaknr = List.of(
				lagArbeidssakMedOrgnr(1L, ORGNR_1, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(2L, ORGNR_1, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(3L, ORGNR_1, APPLIKASJON_UTEN_FAGSAKNR, null)
		);

		List<List<Arbeidssak>> grupperteArbeidssaker = grupperArbeidssakerPerOrgnr(arbeidssakerUtenFagsaknr);

		assertThat(grupperteArbeidssaker).hasSize(3)
				.extracting(List::size)
				.containsOnly(1);
	}

	@Test
	void skalGruppereSakerMedOrgnr() {
		List<Arbeidssak> arbeidssaker = List.of(
				lagArbeidssakMedOrgnr(1L, ORGNR_1, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(2L, ORGNR_2, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(3L, ORGNR_1, APPLIKASJON_MED_FAGSAKNR, "2222"),
				lagArbeidssakMedOrgnr(4L, ORGNR_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedOrgnr(5L, ORGNR_3, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(6L, ORGNR_2, APPLIKASJON_UTEN_FAGSAKNR, null),
				lagArbeidssakMedOrgnr(7L, ORGNR_2, APPLIKASJON_MED_FAGSAKNR, "1234"),
				lagArbeidssakMedOrgnr(8L, ORGNR_2, APPLIKASJON_MED_FAGSAKNR, "2345"),
				lagArbeidssakMedOrgnr(9L, ORGNR_2, APPLIKASJON_MED_FAGSAKNR, "1234"),
				lagArbeidssakMedOrgnr(10L, ORGNR_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedOrgnr(11L, ORGNR_1, APPLIKASJON_MED_FAGSAKNR, "3333"),
				lagArbeidssakMedOrgnr(12L, ORGNR_3, APPLIKASJON_MED_FAGSAKNR, "2345")
		);

		List<List<Arbeidssak>> grupperteArbeidssaker = grupperArbeidssakerPerOrgnr(arbeidssaker);

		assertThat(grupperteArbeidssaker).hasSize(9)
				.extracting(List::size)
				.containsAll(List.of(1, 1, 1, 1, 1, 1, 1, 2, 3));
	}

	private Arbeidssak lagArbeidssakMedAktoerId(Long sakId, String aktoerId, String applikasjon, String fagsaknr) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon(applikasjon)
				.fagsaknr(fagsaknr)
				.aktoerId(aktoerId)
				.build();
	}

	private Arbeidssak lagArbeidssakMedOrgnr(Long sakId, String ornr, String applikasjon, String fagsaknr) {
		return Arbeidssak.builder()
				.sakId(sakId)
				.applikasjon(applikasjon)
				.fagsaknr(fagsaknr)
				.orgnr(ornr)
				.build();
	}

}