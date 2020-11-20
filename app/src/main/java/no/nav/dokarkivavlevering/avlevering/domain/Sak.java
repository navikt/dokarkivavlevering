package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class Sak {
	private final UUID uuid = UUID.randomUUID();
	private Long id;
	private String tema;
	@ToString.Exclude
	private String opprettetAv;
	private Date opprettetTidspunkt;

	@ToString.Exclude
	private Bruker bruker;
	private List<Journalpost> journalposter;

	public Sak tilhoererBruker(Bruker bruker) {
		return this.toBuilder()
				.bruker(bruker).build();
	}
}
