package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.NavnMedGyldighet;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkivavlevering.avlevering.AvleveringSakBerikerMapper.AUTOMATISK_JOBB;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.I;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.N;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.JournalpostType.U;
import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.toLocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaksmappeMapperTest {

	public static final String FIL = "Hello World";
	public static final String BRUKER_ID = "12345678911";
	public static final ZonedDateTime SAK_OPPRETTET_TIDSPUNKT = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2019-10-28T11:41:36.673")).atZone(ZoneId.of("Europe/Oslo"));
	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper(new JournaldatoMapper());

	@Test
	void shouldMap() {
		Sak sak = generateSak("KTR");

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		//saksmappe
		assertEquals("2019", saksmappe.getSaksaar().toString());
		assertEquals("1234567011", saksmappe.getSakssekvensnummer().toString());
		assertEquals(saksmappe.getSaksdato(), toLocalDateTime("2019-10-28 11:41:36").toLocalDate());
		assertEquals("Ukjent", saksmappe.getAdministrativEnhet());
		assertEquals("Ukjent", saksmappe.getSaksansvarlig());
		assertEquals("Under behandling", saksmappe.getSaksstatus());
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
		assertEquals("1234567011", saksmappe.getMappeID());
		assertEquals("Kontroll", saksmappe.getTittel());
		assertThat(saksmappe.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2019-10-28 11:41:36"));
		assertEquals("Automatisk jobb", saksmappe.getOpprettetAv());
		assertEquals(1, saksmappe.getParts().size());
		assertEquals(1, saksmappe.getRegistrerings().size());
		//saksmappe/part
		assertPart(saksmappe.getParts().get(0));

		assertRegistrering((no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0));
	}

	@Test
	void shouldNotMapSendtDatoWhenJournalpostTypeIsNotU() {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost(I.name()).toBuilder()
						.opprettetAv(null)
						.opprettetAvNavn(null)
						.opprettetAvBeriketNavn(null)
						.dok(Collections.singletonList(generateDokumentInfo()))
						.build())).build();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		List<Dokumentobjekt> dokument = saksmappe.getRegistrerings().get(0).getDokumentbeskrivelses().get(0).getDokumentobjekts();
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost journalpost = (no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0);
		assertEquals(1, dokument.size());
		assertNull(journalpost.getSendtDato());
	}

	@Test
	void shouldMapWithoutDokument() {
		Sak sak = generateSak("VEN");

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		List<Dokumentobjekt> dokument = saksmappe.getRegistrerings().get(0).getDokumentbeskrivelses().get(0).getDokumentobjekts();
		assertEquals(0, dokument.size());
	}

	@Test
	void shouldMapOpprettetAvToBeriketNavnWhenOpprettetAvNavnIsNull() {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost(U.name()).toBuilder()
						.opprettetAv("A000000")
						.opprettetAvNavn(null)
						.opprettetAvBeriketNavn("Saksbehandler Sakbehandlerstad")
						.dok(Collections.singletonList(generateDokumentInfo()))
						.build())).build();
		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		final Registrering registrering = saksmappe.getRegistrerings().get(0);
		assertThat(registrering.getOpprettetAv()).isEqualTo("Saksbehandler Sakbehandlerstad");
	}

	@Test
	void shouldMapOpprettetAvUkjentWhenOpprettetAvIsNull() {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost(U.name()).toBuilder()
						.opprettetAv(null)
						.opprettetAvNavn(null)
						.opprettetAvBeriketNavn(null)
						.dok(Collections.singletonList(generateDokumentInfo()))
						.build())).build();
		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		final Registrering registrering = saksmappe.getRegistrerings().get(0);
		assertThat(registrering.getOpprettetAv()).isEqualTo(Bruker.UKJENT_PERSON);
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapFileExtensionCorrect(String fileType, String forventetResultat) {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
						generateJournalpost(U.name()).toBuilder()
								.opprettetAv(null)
								.opprettetAvNavn(null)
								.opprettetAvBeriketNavn(null)
								.dok(Collections.singletonList(
										generateDokumentInfo().toBuilder()
												.fd(List.of(generateFilDetaljer().toBuilder()
														.filtype(fileType)
														.build()))
												.build()))
								.build()))
				.build();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		var dokObjekt = saksmappe.getRegistrerings().getFirst().getDokumentbeskrivelses().getFirst().getDokumentobjekts().getFirst();
		assertEquals(dokObjekt.getFormat(), forventetResultat);
		assertTrue(getFileExtension(dokObjekt.getReferanseDokumentfil()).equalsIgnoreCase(forventetResultat));

	}

	public static Stream<Arguments> shouldMapFileExtensionCorrect() {
		return Stream.of(
				Arguments.of("PDFA", "PDF"),
				Arguments.of("PDF", "PDF"),
				Arguments.of("JPEG", "JPEG"),
				Arguments.of("TIFF", "TIFF"),
				Arguments.of("JSON", "JSON"),
				Arguments.of("XLSX", "XLSX"),
				Arguments.of("RTF", "RTF"),
				Arguments.of("XML", "XML"),
				Arguments.of("AXML", "AXML")
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapJorunalstatusWithDecode(String journalstatus, String decode) {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
						generateJournalpost(U.name()).toBuilder()
								.opprettetAv(null)
								.opprettetAvNavn(null)
								.opprettetAvBeriketNavn(null)
								.status(journalstatus)
								.build()))
				.build();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		var journalpost = (no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().getFirst();

		assertEquals(journalpost.getJournalstatus(), decode);
	}

	public static Stream<Arguments> shouldMapJorunalstatusWithDecode() {
		return Stream.of(
				Arguments.of("J", "Journalført"),
				Arguments.of("FS", "Ferdig og klar for sentral utskrift"),
				Arguments.of("FL", "Ferdig og klar for lokal utskrift"),
				Arguments.of("E", "Ekspedert")
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapAdministrativEnhetFromSakAdministrativEnhet(String administrativEnhet, String administrativEnhetTema, String resultat) {
		final Sak sak = generateSak().toBuilder()
				.administrativEnhet(administrativEnhet)
				.administrativEnhetTema(administrativEnhetTema)
				.build();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		assertEquals(saksmappe.getAdministrativEnhet(), resultat);
	}

	private static Stream<Arguments> shouldMapAdministrativEnhetFromSakAdministrativEnhet() {
		return Stream.of(
				Arguments.of("Nav Arbeid- og ytelser", null, "Nav Arbeid- og ytelser"),
				Arguments.of(null, "Nav Arbeid- og ytelser", "Nav Arbeid- og ytelser"),
				Arguments.of(null, null, "Ukjent"),
				Arguments.of("Nav Arbeid- og ytelser", "Annen Administrativ Enhet", "Nav Arbeid- og ytelser")
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapKorrespondansepart(String journalpostType, String opprettetAvNavn, String opprettetAv,
									 String opprettetAvBeriketNavn, String saksbehandler, String korrespondanseParttype) {
		final Sak sak = generateSak().toBuilder()
				.opprettetAv(opprettetAv)
				.jp(List.of(generateJournalpost(journalpostType).toBuilder()
						.opprettetAv(opprettetAv)
						.opprettetAvBeriketNavn(opprettetAvBeriketNavn)
						.opprettetAvNavn(opprettetAvNavn)
						.build()))
				.build();

		Saksmappe map = saksmappeMapper.map(sak);

		Korrespondansepart korrespondansepart = map.getRegistrerings().getFirst().getKorrespondanseparts().get(0);

		assertEquals(saksbehandler, korrespondansepart.getSaksbehandler());
		assertEquals(saksbehandler, korrespondansepart.getSaksbehandler());
		assertEquals(korrespondanseParttype, korrespondansepart.getKorrespondanseparttype());

	}

	private static Stream<Arguments> shouldMapKorrespondansepart() {
		return Stream.of(
				Arguments.of(N.name(), "Impulsiv Harmoni", null, null, "Impulsiv Harmoni", "Intern mottaker"),
				Arguments.of(N.name(), "teamdokumenthandtering:dokdistfordeling", null, null, AUTOMATISK_JOBB, "Intern mottaker"),
				Arguments.of(U.name(), null, "teamdokumenthandtering:dokdistfordeling", null, AUTOMATISK_JOBB, "Mottaker"),
				Arguments.of(I.name(), null, null, null, "Ukjent", "Avsender"),
				Arguments.of(I.name(), null, null, "Bjarne Betjent", "Bjarne Betjent", "Avsender")
		);
	}

	public static String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1);
		}
		return "";
	}

	private void assertPart(Part part) {
		assertEquals("12345678911", part.getPartID());
		assertEquals("Frank", part.getPartNavn());
		assertEquals("Bruker", part.getPartRolle());
	}

	private void assertRegistrering(no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering) {
		assertEquals("2020", registrering.getJournalaar().toString());
		assertEquals("453637481", registrering.getJournalsekvensnummer().toString());
		assertEquals("453637481", registrering.getJournalpostnummer().toString());
		assertEquals("Utgående dokument", registrering.getJournalposttype());
		assertEquals("Ferdig og klar for sentral utskrift", registrering.getJournalstatus());
		assertEquals(registrering.getJournaldato(), toLocalDateTime("2020-11-10 16:04:43").toLocalDate());
		assertEquals(registrering.getDokumentetsDato(), toLocalDateTime("2020-11-10 16:05:43").toLocalDate());
		assertThat(registrering.getSystemID().getValue()).isNotEmpty();
		assertThat(registrering.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals("srvmelosys", registrering.getOpprettetAv());
		assertEquals("453637481", registrering.getRegistreringsID());
		assertEquals("Legg til ny institusjon", registrering.getTittel());
		assertEquals(1, registrering.getKorrespondanseparts().size());
		assertEquals(1, registrering.getDokumentbeskrivelses().size());

		assertKorrespondanseparts(registrering.getKorrespondanseparts().get(0));
		assertDokumentBeskrivelse(registrering.getDokumentbeskrivelses().get(0));
	}

	private void assertDokumentObjekt(Dokumentobjekt dokObjekt) {
		assertFalse(dokObjekt.getSystemID().getValue().isEmpty());
		assertEquals(dokObjekt.getVersjonsnummer(), toBigInteger(1));
		assertEquals("Arkivformat", dokObjekt.getVariantformat());
		assertEquals("PDF", dokObjekt.getFormat());
		assertThat(dokObjekt.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals("Automatisk jobb", dokObjekt.getOpprettetAv());
		assertEquals("DOKUMENTER/KTR/453637481_55c39cdb-f052-4f4e-a9a5-900b455ca915.pdf", dokObjekt.getReferanseDokumentfil());
		assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", dokObjekt.getSjekksum());
		assertEquals("SHA-256", dokObjekt.getSjekksumAlgoritme());
		assertEquals(dokObjekt.getFilstoerrelse(), toBigInteger(FIL.length()));
	}

	private void assertKorrespondanseparts(Korrespondansepart korrPart) {
		assertEquals("Mottaker", korrPart.getKorrespondanseparttype());
		assertEquals("Bruker Brukersen", korrPart.getKorrespondansepartNavn());
		assertEquals("srvmelosys", korrPart.getSaksbehandler());
	}

	private void assertDokumentBeskrivelse(Dokumentbeskrivelse dok) {
		assertFalse(dok.getSystemID().getValue().isEmpty());
		assertEquals("Strukturert elektronisk dokument", dok.getDokumenttype());
		assertEquals("Dokumentet er ferdigstilt", dok.getDokumentstatus());
		assertEquals("Legg til ny institusjon", dok.getTittel());
		assertThat(dok.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals("Automatisk jobb", dok.getOpprettetAv());
		assertEquals("HOVEDDOKUMENT", dok.getTilknyttetRegistreringSom());
		assertEquals(dok.getDokumentnummer(), toBigInteger(454017976));
		assertThat(dok.getTilknyttetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals("Automatisk jobb", dok.getTilknyttetAv());
		assertEquals(1, dok.getDokumentobjekts().size());

		assertDokumentObjekt(dok.getDokumentobjekts().get(0));
	}

	private BigInteger toBigInteger(int smallInteger) {
		return new BigInteger(String.valueOf(smallInteger));
	}

	private Sak generateSak() {
		return generateSak("KTR");
	}

	private Sak generateSak(String tema) {
		return Sak.builder()
				.id((long) 1234567011)
				.tema(tema)
				.bruker(new Bruker(BRUKER_ID, null))
				.brukerMedNavnedata(generateBrukerMedNavnedata())
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(SAK_OPPRETTET_TIDSPUNKT.toLocalDateTime())
				.opprettetAvBeriketNavn("Automatisk jobb")
				.jp(List.of(generateJournalpost(U.name()))).build();
	}

	private BrukerMedNavnedata generateBrukerMedNavnedata() {
		return new BrukerMedNavnedata(BRUKER_ID, List.of(
				new NavnMedGyldighet(SAK_OPPRETTET_TIDSPUNKT.minusYears(10), SAK_OPPRETTET_TIDSPUNKT.minusYears(2), "Foreldet"),
				new NavnMedGyldighet(SAK_OPPRETTET_TIDSPUNKT.minusYears(2), SAK_OPPRETTET_TIDSPUNKT.plusMonths(2), "Frank"),
				new NavnMedGyldighet(SAK_OPPRETTET_TIDSPUNKT.plusMonths(2), null, "For nytt")));
	}

	private Journalpost generateJournalpost(String journalpostType) {
		return Journalpost.builder()
				.id((long) 453637481)
				.type(journalpostType)
				.status("FS")
				.innhold("Legg til ny institusjon")
				.avsenderMottaker("Bruker Brukersen")
				.datoMottatt(null)
				.datoDokument(toLocalDateTime("2020-11-10 16:05:43.332"))
				.datoJournal(toLocalDateTime("2020-11-10 16:04:43.35"))
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.338"))
				.datoEkspedert(null)
				.datoSendtPrint(null)
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.opprettetAvNavn("srvmelosys")
				.endretAv("srvmelosys")
				.endretAvBeriketNavn("Bjarne Betjent")
				.dok(Collections.singletonList(generateDokumentInfo()))
				.build();
	}

	private DokumentInfo generateDokumentInfo() {
		return DokumentInfo.builder()
				.id((long) 454017976)
				.relTilknyttetSom("HOVEDDOKUMENT")
				.relDatoOpprettet(toLocalDateTime("2020-11-10 16:04:43.343"))
				.relOpprettetAv("srvmelosys")
				.relOpprettetAvBeriketNavn("Automatisk jobb")
				.kategoriDecode("Strukturert elektronisk dokument")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.fd(List.of(generateFilDetaljer()))
				.build();
	}

	private FilDetaljer generateFilDetaljer() {
		return FilDetaljer.builder()
				.id((long) 539876247)
				.filUuid("55c39cdb-f052-4f4e-a9a5-900b455ca915")
				.filtype("PDF")
				.fil(FIL.getBytes())
				.filstorrelseBeriket(FIL.length())
				.sha256hashBeriket("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e")
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.343"))
				.opprettetAv("srvRuting")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.build();
	}

}