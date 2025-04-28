package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.utils;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Arkivsak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AvsluttSakUtils {

	public static void oppdaterArbeidsstatusForArkivsak(Arkivsak arkivsak, Arbeidsstatus arbeidsstatus) {
		//TODO: Ved oppdatering til en endelig status burde vi endre vanlig status også, ikke bare arbeidsstatus
		arkivsak.arbeidssaker().forEach(arbeidssak -> arbeidssak.setArbeidsstatus(arbeidsstatus));
	}

	public static List<List<Arbeidssak>> grupperArbeidssakerPerAktoerId(List<Arbeidssak> arbeidssaker) {
		return new ArrayList<>(arbeidssaker.stream()
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getAktoerId(),
						arbeidssak.getApplikasjon(),
						//TODO: Ikke grupper de som mangler fagsaknr
						arbeidssak.getFagsaknr() == null ? "" : arbeidssak.getFagsaknr()
				)))
				.values()
		);
	}

	public static List<List<Arbeidssak>> grupperArbeidssakerPerOrgnr(List<Arbeidssak> arbeidssaker) {
		return new ArrayList<>(arbeidssaker.stream()
				.collect(Collectors.groupingBy(arbeidssak -> List.of(
						arbeidssak.getOrgnr(),
						arbeidssak.getApplikasjon(),
						//TODO: Ikke grupper de som mangler fagsaknr
						arbeidssak.getFagsaknr() == null ? "" : arbeidssak.getFagsaknr()
				)))
				.values()
		);
	}

}