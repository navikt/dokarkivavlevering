package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Arkivendring {
	private final Long id;
	private final String element;
	private final Date tidspunkt;
	@ToString.Exclude
	private final String utfoertAv;
	private final String fraVerdi;
	private final String tilVerdi;
}
