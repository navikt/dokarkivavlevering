package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
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
}
