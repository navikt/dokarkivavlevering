package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;

public class ReferanseMetadataMapper {

	public static String mapArkivElementToReferanseMetadata(Arkivendring arkivendring) {
		var mappedArkivElement = mapArkivElementToReferanseMetadataInternal(arkivendring);
		if (mappedArkivElement == null) {
			throw new IllegalArgumentException();
		}
		return mappedArkivElement;
	}

	public static boolean arkivElementSkalIkkeAvleveres(Arkivendring arkivendring) {
		return mapArkivElementToReferanseMetadataInternal(arkivendring) == null;
	}

	private static String mapArkivElementToReferanseMetadataInternal(Arkivendring arkivendring) {
		return switch (arkivendring.getElement()) {
			case "Journalpost.innhold", "DokumentInfo.tittel" -> "M020";
			case "Journalpost.journalpostId" -> "M004";
			case "Saksrelasjon.sakId" -> "M003";
			case "Journalpost.journalpostStatus" -> "M053";
			case "Journalpost.journalfoertAvNavn" -> "M601";
			case "JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom" -> "M217";
			case "DokumentInfo.dokumentInfoId" -> "M007";
			case "Journalpost.fagomrade" -> "M002";
			case "Journalpost.avsend_mottaker" -> "M400";
			case null, default -> null;
		};
	}
}
