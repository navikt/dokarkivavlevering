package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentInfo {
	private final Long id;
	private final String relasjonTilknyttetSom;
	private final Date relasjonDatoOpprettet;
	@ToString.Exclude
	private final String relasjonOpprettetAv;
	private final String kategori;
	private final String status;
	@ToString.Exclude
	private final String tittel;
	private final Date datoOpprettet;
	@ToString.Exclude
	private final String opprettetAv;
	private final List<FilDetaljer> fildetaljer;

	public String getOpprettetAv() {
		return opprettetAv;
	}
}
