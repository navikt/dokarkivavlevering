package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;

import java.util.List;

public record Arkivsak(
		List<Arbeidssak> arbeidssaker,
		List<Journalpost> journalposter
) {
	public List<Long> getArbeidssaksIder() {
		return arbeidssaker.stream()
				.map(Arbeidssak::getSakId)
				.toList();
	}

	public String getApplikasjon() {
		return arbeidssaker.get(0).getApplikasjon();
	}

	@Override
	public String toString() {
		return "Arkivsak{" + getArbeidssaksIder() +
				'}';
	}
}
