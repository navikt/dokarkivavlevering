package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
	public static final String UKJENT_PERSON = "Ukjent navn";
	public static final String UKJENT_ORGANISASJON = "Ukjent organisasjon";
	@ToString.Exclude
	private final String id;
	@ToString.Exclude
	private final String navn;

	public boolean isPerson() {
		return !isOrganisasjon();
	}

	public boolean isOrganisasjon() {
		return isNumeric(id) && id.length() == 9;
	}

	public static Bruker ukjentOrganisasjon(final String id) {
		return new Bruker(id, UKJENT_ORGANISASJON);
	}

	public static Bruker ukjentPerson(final String id) {
		return new Bruker(id, UKJENT_PERSON);
	}
}
