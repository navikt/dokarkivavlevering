package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.SimpleNavn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

@Component
@Profile("genererAvlevering")
public class EregService {

	private final EregConsumer eregConsumer;

	public EregService(EregConsumer eregConsumer) {
		this.eregConsumer = eregConsumer;
	}

	// TODO: Bruke endepunktet /v2/organisasjon/hentOrganisasjoner for å hente alle navnene på én gang?
	public Map<String, BrukerMedNavnedata> lagMapMellomOrgnrOgOrgnavn(Set<String> organisasjonsnummer) {
		return organisasjonsnummer.stream()
				.map(orgnr -> {
					String organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(orgnr);
					return new BrukerMedNavnedata(orgnr, singletonList(new SimpleNavn(organisasjonsnavn)));
				})
				.collect(toMap(BrukerMedNavnedata::getId, bruker -> bruker));
	}
}
