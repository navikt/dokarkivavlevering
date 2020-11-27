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
public class Journalpost {
	private final UUID uuid = UUID.randomUUID();
	private final Long id;
	private final String type;
	private final String status;
	@ToString.Exclude
	private final String innhold;
	@ToString.Exclude
	private final String avsenderMottaker;
	private final Date datoMottatt;
	private final Date datoDokument;
	private final Date datoJournal;
	private final Date datoOpprettet;
	private final Date datoEkspedert;
	private final Date datoSendtPrint;
	@ToString.Exclude
	private final String opprettetAv;
	@ToString.Exclude
	private final String opprettetAvBeriketNavn;
	@ToString.Exclude
	private final String opprettetAvNavn;
	@ToString.Exclude
	private final String endretAv;
	@ToString.Exclude
	private final String endretAvBeriketNavn;
	private final List<DokumentInfo> dokumenter;
	private final List<Arkivendring> arkivendringer;
}
