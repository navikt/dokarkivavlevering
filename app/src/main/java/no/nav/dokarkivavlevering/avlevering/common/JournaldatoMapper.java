package no.nav.dokarkivavlevering.avlevering.common;

import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournaldatoMapper {

	public Date mapJournaldato(final Journalpost journalpost) {
		if (journalpost.getDatoJournal() == null) {
			return mapFallbackDatoDokument(journalpost);
		}
		return journalpost.getDatoJournal();
	}

	private Date mapFallbackDatoDokument(final Journalpost journalpost) {
		if (journalpost.getDatoDokument() == null) {
			return mapFallbackDatoDokumentFerdig(journalpost);
		}
		return journalpost.getDatoDokument();
	}

	private Date mapFallbackDatoDokumentFerdig(final Journalpost journalpost) {
		final Optional<DokumentInfo> hoveddokument = journalpost.getDok()
				.stream().filter(d -> "HOVEDDOKUMENT".equals(d.getRelTilknyttetSom()))
				.findFirst();
		if (hoveddokument.isPresent()) {
			final DokumentInfo dokumentInfo = hoveddokument.get();
			if (dokumentInfo.getDatoFerdig() == null) {
				return journalpost.getDatoEndret();
			} else {
				return dokumentInfo.getDatoFerdig();
			}
		} else {
			// Ikke et hoveddokument å falle tilbake på
			return null;
		}
	}
}
