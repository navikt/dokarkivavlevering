package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AvsluttSakUtils {

	public static void oppdaterArbeidsstatusForArkivsak(Arkivsak arkivsak, Arbeidsstatus arbeidsstatus) {
		arkivsak.arbeidssaker().forEach(arbeidssak -> arbeidssak.setArbeidsstatus(arbeidsstatus));
	}

	public static List<List<Arbeidssak>> grupperArbeidssakerPerAktoerId(List<Arbeidssak> arbeidssaker) {
		Collection<List<Arbeidssak>> arbeidssakerUtenFagsakNr = getArbeidssakerUtenFagsakNrForAktoerId(arbeidssaker);

		Collection<List<Arbeidssak>> arbeidssakerMedFagsaknr = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getAktoerId(),
						arbeidssak.getApplikasjon(),
						arbeidssak.getFagsaknr()
				)))
				.values();

		ArrayList<List<Arbeidssak>> grupperteArbeidssakerPerAktoerId = new ArrayList<>();
		grupperteArbeidssakerPerAktoerId.addAll(arbeidssakerUtenFagsakNr);
		grupperteArbeidssakerPerAktoerId.addAll(arbeidssakerMedFagsaknr);
		return grupperteArbeidssakerPerAktoerId;
	}

	public static List<List<Arbeidssak>> grupperArbeidssakerPerOrgnr(List<Arbeidssak> arbeidssaker) {
		Collection<List<Arbeidssak>> arbeidssakerUtenFagsakNr = getArbeidssakerUtenFagsakNrForOrgNr(arbeidssaker);

		Collection<List<Arbeidssak>> arbeidssakermedFagsaknr = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getOrgnr(),
						arbeidssak.getApplikasjon(),
						arbeidssak.getFagsaknr()
				)))
				.values();

		ArrayList<List<Arbeidssak>> grupperteArbeidssakerPerOrgnr = new ArrayList<>();
		grupperteArbeidssakerPerOrgnr.addAll(arbeidssakerUtenFagsakNr);
		grupperteArbeidssakerPerOrgnr.addAll(arbeidssakermedFagsaknr);
		return grupperteArbeidssakerPerOrgnr;
	}

	private static Collection<List<Arbeidssak>> getArbeidssakerUtenFagsakNrForAktoerId(List<Arbeidssak> arbeidssaker) {
		var grupperteArbeidssakerPerAktoerId = new ArrayList<List<Arbeidssak>>();

		// Arbeidssaker uten både fagsaknr og applikasjon blir gruppert i hver sine arkivsaker
		var arbeidssakerUtenApplikasjon = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() == null)
				.filter(arbeidssak -> arbeidssak.getApplikasjon() == null)
				.toList();
		arbeidssakerUtenApplikasjon.forEach(arbeidssak -> grupperteArbeidssakerPerAktoerId.add(List.of(arbeidssak)));

		var arbeidssakerMedApplikasjon =  arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() == null)
				.filter(arbeidssak -> arbeidssak.getApplikasjon() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getAktoerId(),
						arbeidssak.getApplikasjon()
				)))
				.values();
		grupperteArbeidssakerPerAktoerId.addAll(arbeidssakerMedApplikasjon);

		return grupperteArbeidssakerPerAktoerId;
	}

	private static Collection<List<Arbeidssak>> getArbeidssakerUtenFagsakNrForOrgNr(List<Arbeidssak> arbeidssaker) {
		var grupperteArbeidssakerPerOrgnr = new ArrayList<List<Arbeidssak>>();

		// Arbeidssaker uten både fagsaknr og applikasjon blir gruppert i hver sine arkivsaker
		var arbeidssakerUtenApplikasjon = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() == null)
				.filter(arbeidssak -> arbeidssak.getApplikasjon() == null)
				.toList();
		arbeidssakerUtenApplikasjon.forEach(arbeidssak -> grupperteArbeidssakerPerOrgnr.add(List.of(arbeidssak)));

		var arbeidssakerMedApplikasjon = arbeidssaker.stream()
				.filter(arbeidssak -> arbeidssak.getFagsaknr() == null)
				.filter(arbeidssak -> arbeidssak.getApplikasjon() != null)
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getOrgnr(),
						arbeidssak.getApplikasjon()
				)))
				.values();
		grupperteArbeidssakerPerOrgnr.addAll(arbeidssakerMedApplikasjon);

		return grupperteArbeidssakerPerOrgnr;
	}
}