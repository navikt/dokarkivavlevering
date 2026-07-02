package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journalpost {
	private static final String JOURNALPOSTSTATUS_RESERVERT = "R";
	private LocalDateTime opprettetdato;

	private LocalDate journaldato;

	private String journalstatus;

	private String journalfoerendeEnhet;

	private boolean erFeilregistrert;

	//Journalposter i status R har ikke journaldato, returnerer derfor opprettetDato for disse og journaldato for resten
	/**
	 * @return opprettetdato hvis journalstatus er "R", ellers journaldato
	 */
	public LocalDate getJournalDatoOrOpprettetDato() {
		return JOURNALPOSTSTATUS_RESERVERT.equals(journalstatus) ? getOpprettetdato().toLocalDate() : getJournaldato();
	}
}