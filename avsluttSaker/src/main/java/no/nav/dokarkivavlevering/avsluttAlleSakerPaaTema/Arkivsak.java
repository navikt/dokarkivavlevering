package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;

import java.util.List;

public record Arkivsak(
		List<Arbeidssak> arbeidssaker
) {
	public List<Long> getArbeidssaksIder() {
		return arbeidssaker.stream()
				.map(Arbeidssak::getSakId)
				.toList();
	}
}
