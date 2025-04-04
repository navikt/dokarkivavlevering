package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Data
public class Journalpost {

	private String journalstatus;

	private boolean erFeilregistrert;

	private String journalfoerendeEnhet;

	@Temporal(TemporalType.TIMESTAMP)
	private Date journaldato;



}
