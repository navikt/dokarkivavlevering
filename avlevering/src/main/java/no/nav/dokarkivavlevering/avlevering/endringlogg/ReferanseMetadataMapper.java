package no.nav.dokarkivavlevering.avlevering.endringlogg;

public class ReferanseMetadataMapper {
	public static String mapArkivElementToReferanseMetadata(String arkivElement) {
		return switch (arkivElement) {
			case "Journalpost.innhold", "DokumentInfo.tittel" -> "M020";
			case "Journalpost.journalpostId" -> "M004";
			case "Saksrelasjon.sakId" -> "M003";
			case "Journalpost.journalpostStatus" -> "M053";
			case "Journalpost.journalfoertAvNavn" -> "M601";
			case "JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom" -> "M217";
			case "DokumentInfo.dokumentInfoId" -> "M007";
			case "Journalpost.fagomrade" -> "M002";
			case "Journalpost.avsend_mottaker" -> "M400";
			case null, default -> "N/A";
		};
	}
}
