package no.nav.dokarkivavlevering.avlevering.dokument;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Dokument {
	private final String journalpostId;
	private final String filUuid;
	private final byte[] fil;
}
