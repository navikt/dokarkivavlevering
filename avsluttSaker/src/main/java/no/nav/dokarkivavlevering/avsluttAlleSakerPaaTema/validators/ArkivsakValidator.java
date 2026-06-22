package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;

import java.util.List;
import java.util.Set;

public class ArkivsakValidator {

	private static final Set<String> AAPNE_JOURNALSTATUSER = Set.of("M", "MO", "OD", "D");
	public static final Set<String> LUKKEDE_JOURNALSTATUSER = Set.of("FL", "FS", "E", "J", "R");

	// hvis det finnes noen journalposter knyttet til arkivsak med status M, MO, OD, R eller D og som ikke er feilregistrert så skriv feilmelding til logg (kan ikke avslutte arkivsak med åpne journalposter) og gå til neste arkivsak
	public static boolean harArkivsakEnAapenJournalpost(List<Journalpost> journalposter) {
		return journalposter.stream()
				.anyMatch(ArkivsakValidator::erJournalpostAapenOgIkkeFeilregistrert);
	}

	// Hvis det ikke finnes noen journalposter knyttet til arkivsak med status FL, FS, E eller J og som ikke er feilregistrert så oppdater tilhørende rader i SAK ihht. 3.1.1 og gå til neste arkivsak
	public static boolean harArkivsakFerdigstilteJournalposter(List<Journalpost> journalposter) {
		return journalposter.stream()
				.anyMatch(ArkivsakValidator::erJournalpostFerdigstiltOgIkkeFeilregistrert);
	}

	private static boolean erJournalpostAapenOgIkkeFeilregistrert(Journalpost journalpost) {
		return erJournalstatusAapen(journalpost) && !journalpost.isErFeilregistrert();
	}

	private static boolean erJournalpostFerdigstiltOgIkkeFeilregistrert(Journalpost journalpost) {
		return erJournalstatusLukket(journalpost) && !journalpost.isErFeilregistrert();
	}

	private static boolean erJournalstatusAapen(Journalpost journalpost) {
		return AAPNE_JOURNALSTATUSER.contains(journalpost.getJournalstatus());
	}

	private static boolean erJournalstatusLukket(Journalpost journalpost) {
		return LUKKEDE_JOURNALSTATUSER.contains(journalpost.getJournalstatus());
	}
}
