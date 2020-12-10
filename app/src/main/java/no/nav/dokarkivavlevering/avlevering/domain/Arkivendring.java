package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class Arkivendring {
	public static final String INGEN_VERDI = "ingen verdi";
	private final Long id;
	private final String element;
	private final Date tidspunkt;
	@ToString.Exclude
	private final String utfoertAv;
	@ToString.Exclude
	private final String utfoertAvBeriketNavn;
	private final String fraVerdi;
	private final String tilVerdi;
}
