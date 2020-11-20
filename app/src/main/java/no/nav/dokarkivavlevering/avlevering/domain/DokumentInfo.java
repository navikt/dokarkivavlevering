package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class DokumentInfo {
	private final Long id;
	private final String relasjonTilknyttetSom;
	private final Date relasjonDatoOpprettet;
	@ToString.Exclude
	private final String relasjonOpprettetAv;
	@ToString.Exclude
	private final String relasjonOpprettetAvBeriketNavn;
	private final String kategori;
	private final String status;
	@ToString.Exclude
	private final String tittel;
	private final Date datoOpprettet;
	@ToString.Exclude
	private final String opprettetAv;
	@ToString.Exclude
	private final String opprettetAvBeriketNavn;
	private final List<FilDetaljer> fildetaljer;
	private final List<Arkivendring> arkivendringer;

	public String getOpprettetAv() {
		return opprettetAv;
	}
}
