package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.SimpleNavn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@Component
@Profile("genererAvlevering")
public class EregService {

	private final EregConsumer eregConsumer;

	public EregService(EregConsumer eregConsumer) {
		this.eregConsumer = eregConsumer;
	}

	public Map<String, BrukerMedNavnedata> hentOrganisasjonBrukere(final Set<String> organisasjonsnummer) {
		return organisasjonsnummer.stream()
				.map(orgnr -> {
					final String navn = eregConsumer.hentNavn(orgnr);
					return new BrukerMedNavnedata(orgnr, singletonList(new SimpleNavn(navn)));
				}).collect(Collectors.toMap(BrukerMedNavnedata::getId, bruker -> bruker));
	}
}
