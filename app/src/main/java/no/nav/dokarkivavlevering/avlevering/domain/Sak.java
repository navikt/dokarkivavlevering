package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Sak {
	private Long id;
	private String tema;
	@ToString.Exclude
	private String opprettetAv;
	private Date opprettetTidspunkt;

	@ToString.Exclude
	private Bruker bruker;
	private List<Journalpost> journalposter;
}
