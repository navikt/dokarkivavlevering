package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Journalpost {

	private LocalDateTime opprettetdato;

	private LocalDateTime journaldato;

	private String journalstatus;

	private String journalfoerendeEnhet;

	private boolean erFeilregistrert;

}