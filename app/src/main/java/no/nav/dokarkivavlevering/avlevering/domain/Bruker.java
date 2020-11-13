package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
	@ToString.Exclude
	private final String id;
}
