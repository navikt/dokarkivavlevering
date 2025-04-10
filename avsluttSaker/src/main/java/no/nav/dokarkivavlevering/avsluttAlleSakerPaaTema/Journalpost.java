package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Journalpost {

	private LocalDateTime opprettetdato;

	private LocalDate journaldato;

	private String journalstatus;

	private String journalfoerendeEnhet;

	private boolean erFeilregistrert;

}