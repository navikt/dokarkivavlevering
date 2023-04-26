package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.NavnMedGyldighet;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class EregService {

	private final EregConsumer eregConsumer;

	public EregService(EregConsumer eregConsumer) {
		this.eregConsumer = eregConsumer;
	}

	public Map<String, BrukerMedNavnedata> hentOrganisasjonBrukere(final Set<String> organisasjonsnummer) {
		return organisasjonsnummer.stream()
				.map(orgnr -> {
					final String navn = eregConsumer.hentNavn(orgnr);
					return new BrukerMedNavnedata(orgnr, Collections.singletonList(new NavnMedGyldighet(null, null, navn)));
				}).collect(Collectors.toMap(BrukerMedNavnedata::getId, bruker -> bruker));
	}
}
