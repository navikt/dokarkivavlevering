package no.nav.dokarkivavlevering.avlevering.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1192")
public enum Tema {
	ERS("ERS", "Erstatning", true, "", "Nav Klageinstans", false),
	AKT("AKT", "Aktivitetsplan med dialoger", true, "", "Test adminEnhet", false),
	BIL("BIL", "temanavn_bil", true, "", "Test adminEnhet", false),
	SAK("SAK", "Saksomkostninger", true, "", "NAV Klageinstans", false),
	ENF("ENF", "Enslig mor eller far", true, "", "Test adminEnhet", false),
	GRU("GRU", "Grunn- og hjelpestønad", true, "", "Test adminEnhet", false),
	OMS("OMS", "Omsorgspenger, pleiepenger og opplæringspenger", false, "", "Test adminEnhet", false),
	KTR("KTR", "Kontroll", true, "Ikke fagsystem", "NAV Kontroll", false),
	AGR("AGR", "Ajourhold grunnopplysninger", true, "Ikke fagsystem", "NAV Økonomi Stønad", false),
	STO("STO", "Regnskap/Utbetaling", true, "Ikke fagsystem", "NAV Økonomi Stønad", false),
	TRK("TRK", "Trekkhåndtering", true, "", "NAV Økonomi Stønad", false),
	FUL("FUL", "Fullmakt", true, "Ikke fagsystem", "Alle enheter", false),
	GEN("GEN", "Generell", true, "Ikke fagsystem", "Alle enheter", false),
	MED("MED", "Medlemskap", true, "", "NAV Medlemskap og avgift", false),
	UFM("UFM", "Unntak fra medlemskap", true, "", "NAV Medlemskap og avgift", false),
	TRY("TRY", "Trygdeavgift", true, "", "NAV Medlemskap og avgift", false),
	SER("SER", "Serviceklager", true, "Ikke fagsystem", "Alle enheter", false),
	RVE("RVE", "Rettferdsvederlag", true, "Ikke fagsystem", "NAV Arbeids- og velferdsdirektoratet, Arbeids- og tjenesteavdelingen", false),
	VEN("VEN", "Ventelønn", true, "", "NAV arbeid og ytelser Kristiania", false),
	RPO("RPO", "Retting av personopplysninger", true, "Ikke fagsystem", "NAV Klageinstans", false),
	OPP("OPP", "Oppfølging", true, "Arena", "Brukers NAV-kontor*", false),
	SAP("SAP", "Sanksjon person", true, "Arena", "Brukers NAV-kontor* og NAV Arbeid og ytelser", false),
	SYM("SYM", "Sykmeldinger", true, "Infotrygd", "Brukers NAV-kontor*", false),
	OPA("OPA", "Oppfølging arbeidsgiver", true, "Arena", "NAV-kontor og NAV Arbeidslivssenter", false),
	REK("REK", "Rekruttering og stilling", true, "", "NAV-kontor", false),
	AAR("AAR", "Aa-registeret", true, "", "NAV registerforvaltning", false),
	PER("PER", "Permittering og masseoppsigelser", true, "", "NAV-kontor", false),
	SAA("SAA", "Sanksjon arbeidsgiver", true, "", "NAV registerforvaltning", false),
	IAR("IAR", "Inkluderende arbeidsliv", true, "", "NAV Arbeidslivssentre", false);
	private final String temakode;
	private final String temanavn;
	private final boolean avlevDokumenter;
	private final String fagsystem;
	private final String adminEnhet;
	private final boolean inkluderIGodkj;

	Tema(String temakode, String temanavn, boolean avlevDokumenter, String fagsystem, String adminEnhet, boolean inkluderIGodkj) {
		this.temakode = temakode;
		this.temanavn = temanavn;
		this.avlevDokumenter = avlevDokumenter;
		this.fagsystem = fagsystem;
		this.adminEnhet = adminEnhet;
		this.inkluderIGodkj = inkluderIGodkj;
	}

	public static List<Tema> getAlleTema() {
		return Arrays.asList(Tema.values().clone());
	}

	public static List<Tema> getAlleTemaMedDokument() {
		return Arrays.stream(Tema.values().clone())
				.filter(Tema::isAvleverDokumenter)
				.collect(Collectors.toList());
	}

	public String getTemakode() {
		return temakode;
	}

	public String getTemanavn() {
		return temanavn;
	}

	public boolean isAvleverDokumenter() {
		return avlevDokumenter;
	}

	public String getFagsystem() {
		return fagsystem;
	}

	public String getAdminEnhet() {
		return adminEnhet;
	}

	public boolean isInkluderIGodkj() {
		return inkluderIGodkj;
	}
}

