package no.nav.dokarkivavlevering.avlevering.endringlogg;

public enum JournalpostStatus {
	J("J", "JOURNALFØRT"),
	M("M", "MOTTATT"),
	U("U", "UTGAAR"),
	D("D", "UNDER_ARBEID"),
	R("R", "RESERVERT"),
	FS("FS", "FERDIGSTILT"),
	FL("FL", "FERDIGSTILT"),
	E("E", "EKSPEDERT"),
	A("A", "AVBRUTT"),
	MO("MO", "MOTTATT"),
	UB("UB", "UKJENT_BRUKER"),
	OD("OD", "OPPLASTING_DOKUMENT");

	public final String statusCode;
	public final String statusDecode;

	JournalpostStatus(String statusCode, String statusDecode) {
		this.statusCode = statusCode;
		this.statusDecode = statusDecode;
	}

}
