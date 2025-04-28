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

	private LocalDateTime opprettetdato;

	private LocalDate journaldato;

	private String journalstatus;

	private String journalfoerendeEnhet;

	private boolean erFeilregistrert;

}