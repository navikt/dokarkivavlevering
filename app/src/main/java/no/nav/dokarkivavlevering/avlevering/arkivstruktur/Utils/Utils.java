package no.nav.dokarkivavlevering.avlevering.arkivstruktur.Utils;


public class Utils {

	//TODO dette må nok endres. Muligens lage en map e.l. med tema -> (adminEnhet og tema-navn)
	public static String getAdministrativEnhetFromTema(String tema){
		switch(tema.toUpperCase()){
			case "OPA":
				return "Oppfølging arbeidsgiver";

			case "REK":
			case "PER":
				return "NAV-kontor";

			case "AGR":
			case "STO":
			case "TRK":
				return "NAV Økonomi Stønad";

			case "AAR":
			case "SAA":
				return "NAV Registerforvaltning";

			case "MED":
			case "UFM":
			case "TRY":
				return "NAV Medlemskap og avgift";

			case "KTR":
				return "NAV kontroll";

			case "ERS":
			case "SAK":
			case "RPO":
				return "NAV Klageinstans";

			case "IAR":
				return "NAV Arbeidslivssentre";

			case "RVE":
				return "NAV Arbeids- og velferdsdirektoratet, Arbeids- og tjenesteavdelingen";

			case "VEN":
				return "NAV Arbeid og ytelser Kristiania";

			//TODO: NAV-kontor* betyr her brukers NAV-kontor. Hvor får vi tak i dette?
			case "SAP":
				return "NAV-kontor* og NAV Arbeid og ytelser";
			//TODO: NAV-kontor* betyr her brukers NAV-kontor. Hvor får vi tak i dette?
			case "OPP":
			case "SYM":
				return "NAV-kontor*";

			case "FUL":
			case "GEN":
			case "SER":
				return "Alle enheter";

			default:
				return "Ingen Administrativ enhet";

		}
	}
}
