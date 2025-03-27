package no.nav.dokarkivavlevering.avlevering.common;

import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Profile("genererAvlevering")
public class JournaldatoMapper {

	public LocalDateTime mapJournaldato(final Journalpost journalpost) {
		if (journalpost.getDatoJournal() == null) {
			return mapFallbackDatoDokument(journalpost);
		}
		return journalpost.getDatoJournal();
	}

	private LocalDateTime mapFallbackDatoDokument(final Journalpost journalpost) {
		if (journalpost.getDatoDokument() == null) {
			return mapFallbackDatoDokumentFerdig(journalpost);
		}
		return journalpost.getDatoDokument();
	}

	private LocalDateTime mapFallbackDatoDokumentFerdig(final Journalpost journalpost) {
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
