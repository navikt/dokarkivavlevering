package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Collections.singletonList;

@Value
public class BrukerMedNavnedata {
	public static final String UKJENT_PERSON = "Ukjent";
	public static final String UKJENT_ORGANISASJON = "Ukjent organisasjon";
	@ToString.Exclude
	private final String id;
	@ToString.Exclude
	private final List<NavnMedGyldighet> navn;

	public static BrukerMedNavnedata ukjentOrganisasjon(final String id) {
		return new BrukerMedNavnedata(id, singletonList(new SimpleNavn(UKJENT_ORGANISASJON)));
	}

	public static BrukerMedNavnedata ukjentPerson(final String id) {
		return new BrukerMedNavnedata(id, singletonList(new SimpleNavn(UKJENT_PERSON)));
	}

	public String getFulltnavn(ZonedDateTime historiskTidspunkt) {
		// finnes det maks ett navn?
		if (getNavn().size() == 1) {
			return getNavn().get(0).getNavn();
		}
		return getNavn().stream()
				.filter(navn -> navn.isValidFor(historiskTidspunkt))
				.map(NavnMedGyldighet::getNavn)
				.findFirst().orElse(UKJENT_PERSON);
	}
}
