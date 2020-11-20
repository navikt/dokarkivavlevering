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
public class Sak {
	private Long id;
	private String tema;
	@ToString.Exclude
	private String opprettetAv;
	@ToString.Exclude
	private String opprettetAvBeriketNavn;
	private Date opprettetTidspunkt;

	@ToString.Exclude
	private Bruker bruker;
	private List<Journalpost> journalposter;

	public Sak tilhoererBruker(final Bruker bruker) {
		return this.toBuilder()
				.bruker(bruker).build();
	}
}
