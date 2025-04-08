package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.Data;

import java.util.Date;

@Data
public class Journalpost {

	private Date opprettetdato;

	private Date journaldato;

	private String journalstatus;

	private String journalfoerendeEnhet;

	private boolean erFeilregistrert;

}