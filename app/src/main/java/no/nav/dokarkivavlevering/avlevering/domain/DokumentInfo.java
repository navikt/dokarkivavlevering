package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class DokumentInfo {

	private final UUID uuid = UUID.randomUUID();
	private final Long id;
	private final String relTilknyttetSom;
	private final Date relDatoOpprettet;
	@ToString.Exclude
	private final String relOpprettetAv;
	@ToString.Exclude
	private final String relOpprettetAvBeriketNavn;
	private final String kategori;
	private final String status;
	@ToString.Exclude
	private final String tittel;
	private final Date datoOpprettet;
	@ToString.Exclude
	private final String opprettetAv;
	@ToString.Exclude
	private final String opprettetAvBeriketNavn;
	private final List<FilDetaljer> fd;
	private final List<Arkivendring> ae;

	public String getOpprettetAv() {
		return opprettetAv;
	}
}
