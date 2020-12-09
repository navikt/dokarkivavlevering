package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class IdRange {
	private final Long journalpostIdMin;
	private final Long journalpostIdMax;
	private final Long sakIdMax;
}
