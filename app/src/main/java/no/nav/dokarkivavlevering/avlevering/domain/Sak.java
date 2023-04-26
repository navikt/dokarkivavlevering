package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class Sak {
	private final UUID uuid = UUID.randomUUID();
	Long id;
	String tema;
	@ToString.Exclude
	String opprettetAv;
	@ToString.Exclude
	String opprettetAvBeriketNavn;
	Date opprettetTidspunkt;
	Fagomrade fagomrade;

	@ToString.Exclude
	Bruker bruker;
	@ToString.Exclude
	BrukerMedNavnedata brukerMedNavnedata;
	List<Journalpost> jp;
}
