package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class FilDetaljer {
	private final Long id;
	private final String filUuid;
	private final Date datoOpprettet;
	@ToString.Exclude
	private final String opprettetAv;
	private final UUID uuid = UUID.randomUUID();
	@ToString.Exclude
	private final String opprettetAvBeriketNavn;
	private final byte[] fil;
	private final int filstorrelseBeriket;
	private final String sha256hashBeriket;
}
