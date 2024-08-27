package no.nav.dokarkivavlevering.avlevering.utils;

import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.config.Tema.AGR;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.ERS;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.IAR;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.OPA;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.REK;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.RVE;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.SAP;

@Component
public class AvleveringUtils {

	public static final String UTGAAENDE = "U";
	public static final String INNGAAENDE = "I";
	public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private static final Map<String, String> fagomradeBeskrivelseLookup = Map.of(
			AGR.name(), "Endring av bankkonto eller midlertidige adresser",
			ERS.name(), "Krav om økonomisk erstatning fordi NAV har gjort en feil",
			IAR.name(), "Intensjonsavtalen om et mer inkluderende arbeidsliv: Samarbeidsavtaler, mål- og handlingsplaner. Noe tilskudd",
			OPA.name(), "Samhandling mellom NAV og arbeidsgivere, utover det som omfattes av øvrige fagområder",
			REK.name(), "Dokumentasjon knyttet til NAVs rekrutteringsbistand til arbeidsgivere",
			RVE.name(), "NAV utreder og belyser saken på forespørsel fra Statens sivilrettsforvaltning",
			SAP.name(), "Vedtak om stans av sykepenger, og behandling av klager og anker"
	);

	public static boolean isStringTemaAvleverMedDokumenter(String tema) {
		return Tema.valueOf(tema.toUpperCase()).isAvleverDokumenter();
	}

	public static boolean isNav(String journalpostType) {
		return INNGAAENDE.equalsIgnoreCase(journalpostType) | UTGAAENDE.equalsIgnoreCase(journalpostType);
	}

	public static String mapKorrespondansepartType(String journalpostType) {
		return INNGAAENDE.equalsIgnoreCase(journalpostType) ? "Avsender" :
				UTGAAENDE.equalsIgnoreCase(journalpostType) ? "Mottaker" : "Intern avsender";
	}

	public static int getYear(LocalDateTime date) {
		return getYear(date.toLocalDate());
	}

	public static int getYear(LocalDate date) {
		if (date == null) {
			return 0;
		}
		return date.getYear();
	}

	public static String temaNavnDecode(String tema) {
		return Tema.valueOf(tema).getTemanavn();
	}

	public static String getFagomradeBeskrivelse(String fagomrade) {
		return fagomradeBeskrivelseLookup.get(fagomrade);
	}

	public static SystemID generateSystemID() {
		return mapSystemID(UUID.randomUUID());
	}

	public static SystemID mapSystemID(UUID uuid) {
		return mapSystemID(uuid.toString());
	}

	public static SystemID mapSystemID(String value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value);
		return systemID;
	}
}
