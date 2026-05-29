package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

public record EregResponse(
		Navn navn
) {

	record Navn(
			String sammensattnavn
	) {
	}
}