package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Lag tester for gruppering av saker
public class AvsluttSakUtils {

	public static void oppdaterArbeidsstatusForArkivsak(Arkivsak arkivsak, Arbeidsstatus arbeidsstatus) {
		arkivsak.arbeidssaker().forEach(arbeidssak -> arbeidssak.setArbeidsstatus(arbeidsstatus));
	}

	public static List<List<Arbeidssak>> grupperArbeidssakerPerAktoerId(List<Arbeidssak> arbeidssaker) {
		List<Arbeidssak> arbeidssakerUtenFagsakNr = getArbeidssakerUtenFagsakNr(arbeidssaker);

		Collection<List<Arbeidssak>> arbeidssakerMedFagsaknr = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getAktoerId(),
						arbeidssak.getApplikasjon(),
						arbeidssak.getFagsaknr()
				)))
				.values();

		ArrayList<List<Arbeidssak>> grupperteArbeidssakerPerAktoerId = new ArrayList<>();
		arbeidssakerUtenFagsakNr.forEach(arbeidssak -> grupperteArbeidssakerPerAktoerId.add(List.of(arbeidssak)));
		grupperteArbeidssakerPerAktoerId.addAll(arbeidssakerMedFagsaknr);
		return grupperteArbeidssakerPerAktoerId;
	}


	public static List<List<Arbeidssak>> grupperArbeidssakerPerOrgnr(List<Arbeidssak> arbeidssaker) {
		List<Arbeidssak> arbeidssakerUtenFagsakNr = getArbeidssakerUtenFagsakNr(arbeidssaker);

		Collection<List<Arbeidssak>> arbeidssakermedFagsaknr = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getOrgnr(),
						arbeidssak.getApplikasjon(),
						arbeidssak.getFagsaknr()
				)))
				.values();

		ArrayList<List<Arbeidssak>> grupperteArbeidssakerPerOrgnr = new ArrayList<>();
		arbeidssakerUtenFagsakNr.forEach(arbeidssak -> grupperteArbeidssakerPerOrgnr.add(List.of(arbeidssak)));
		grupperteArbeidssakerPerOrgnr.addAll(arbeidssakermedFagsaknr);
		return grupperteArbeidssakerPerOrgnr;
	}

	private static List<Arbeidssak> getArbeidssakerUtenFagsakNr(List<Arbeidssak> arbeidssaker) {
		return arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() == null)
				.toList();
	}
}