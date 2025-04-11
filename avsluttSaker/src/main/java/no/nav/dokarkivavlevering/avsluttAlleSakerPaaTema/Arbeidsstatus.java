package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

public enum Arbeidsstatus {
	//saker som er under behandling vil være i en av disse statusene(eller ingen)
	HENTET_FRA_PDL,
	SKAL_IKKE_HENTE_FRA_PDL,
	PROSESSERING_AV_ARKIVSAK_STARTET,
	//endelige statuser, ferdig behandlet
	SAK_AVSLUTTET,
	FERDIG_TOM_ARKIVSAK,
	FEIL_AAPEN_JOURNALPOST,
	PDL_FANT_IKKE_NY_AKTOERID,
	FEIL_INGEN_ADMINISTRATIV_ENHET,
	FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET
}