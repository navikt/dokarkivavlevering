package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.domain.Journalpost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.ArkivsakValidator.harArkivsakEnAapenJournalpost;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.ArkivsakValidator.harArkivsakFerdigstilteJournalposter;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArkivsakValidatorTest {

	@Test
	void skalValidereArkivsak() {
		List<Journalpost> journalposter = List.of(
				lagJournalpost("FL", false),
				lagJournalpost("E", false),
				lagJournalpost("J", false),
				lagJournalpost("FS", false),
				lagJournalpost("FL", true),
				lagJournalpost("E", true),
				lagJournalpost("J", true),
				lagJournalpost("FS", true),
				lagJournalpost("M", true),
				lagJournalpost("MO", true),
				lagJournalpost("OD", true),
				lagJournalpost("R", true),
				lagJournalpost("D", true)
		);

		boolean result = harArkivsakEnAapenJournalpost(journalposter);

		assertFalse(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"M", "MO", "OD", "D"})
	void skalFinneArkivsakMedAapenJournalpost(String journalstatus) {
		List<Journalpost> journalposter = List.of(
				lagJournalpost(journalstatus, false)
		);

		boolean result = harArkivsakEnAapenJournalpost(journalposter);

		assertTrue(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"FL", "E", "J", "FS", "R"})
	void skalFinneFerdigstilteJournalposter(String journalstatus) {
		List<Journalpost> journalposter = List.of(
				lagJournalpost(journalstatus, false)
		);

		boolean result = harArkivsakFerdigstilteJournalposter(journalposter);

		assertTrue(result);
	}

	@Test
	void skalIkkeFinneFerdigstilteJournalposter() {
		List<Journalpost> journalposter = List.of(
				lagJournalpost("FL", true),
				lagJournalpost("E", true),
				lagJournalpost("J", true),
				lagJournalpost("FS", true),
				lagJournalpost("M", true),
				lagJournalpost("MO", true),
				lagJournalpost("OD", true),
				lagJournalpost("R", true),
				lagJournalpost("D", true),
				lagJournalpost("M", false),
				lagJournalpost("MO", false),
				lagJournalpost("OD", false),
				lagJournalpost("D", false)
		);

		boolean result = harArkivsakFerdigstilteJournalposter(journalposter);

		assertFalse(result);
	}

	private Journalpost lagJournalpost(String journalstatus, boolean feilregistrert) {
		Journalpost journalpost = new Journalpost();
		journalpost.setErFeilregistrert(feilregistrert);
		journalpost.setJournalstatus(journalstatus);

		return journalpost;
	}
}