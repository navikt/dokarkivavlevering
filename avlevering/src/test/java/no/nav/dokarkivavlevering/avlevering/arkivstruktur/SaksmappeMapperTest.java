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

import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.toLocalDateTime;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.INNGAAENDE;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.UTGAAENDE;
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
		assertEquals(saksmappe.getSaksaar().toString(), "2019");
		assertEquals(saksmappe.getSakssekvensnummer().toString(), "1234567011");
		assertEquals(saksmappe.getSaksdato(), toLocalDateTime("2019-10-28 11:41:36").toLocalDate());
		assertEquals(saksmappe.getAdministrativEnhet(), "NAV Kontroll");
		assertEquals(saksmappe.getSaksansvarlig(), "Bjarne Betjent");
		assertEquals(saksmappe.getSaksstatus(), "Under behandling");
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
		assertEquals(saksmappe.getMappeID(), "1234567011");
		assertEquals(saksmappe.getTittel(), "Kontroll");
		assertThat(saksmappe.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2019-10-28 11:41:36"));
		assertEquals(saksmappe.getOpprettetAv(), "Automatisk jobb");
		assertEquals(saksmappe.getParts().size(), 1);
		assertEquals(saksmappe.getRegistrerings().size(), 1);
		//saksmappe/part
		assertPart(saksmappe.getParts().get(0));

		assertRegistrering((no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0));
	}

	@Test
	void shouldNotMapSendtDatoWhenJournalpostTypeIsNotU() {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost(INNGAAENDE).toBuilder()
						.opprettetAv(null)
						.opprettetAvNavn(null)
						.opprettetAvBeriketNavn(null)
						.dok(Collections.singletonList(generateDokumentInfo()))
						.build())).build();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		List<Dokumentobjekt> dokument = saksmappe.getRegistrerings().get(0).getDokumentbeskrivelses().get(0).getDokumentobjekts();
		no.arkivverket.standarder.noark5.arkivstruktur.Journalpost journalpost = (no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0);
		assertEquals(dokument.size(), 1);
		assertNull(journalpost.getSendtDato());
	}

	@Test
	void shouldMapWithoutDokument() {
		Sak sak = generateSak("VEN");

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		List<Dokumentobjekt> dokument = saksmappe.getRegistrerings().get(0).getDokumentbeskrivelses().get(0).getDokumentobjekts();
		assertEquals(dokument.size(), 0);
	}

	@Test
	void shouldMapOpprettetAvToBeriketNavnWhenOpprettetAvNavnIsNull() {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost(UTGAAENDE).toBuilder()
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
				generateJournalpost(UTGAAENDE).toBuilder()
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
				generateJournalpost(UTGAAENDE).toBuilder()
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
	public static String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
			// Ensure there's a character after the dot and it's not the first character
			return fileName.substring(dotIndex + 1);
		}
		return ""; // No extension found or invalid format
	}

	private void assertPart(Part part) {
		assertEquals(part.getPartID(), "12345678911");
		assertEquals(part.getPartNavn(), "Frank");
		assertEquals(part.getPartRolle(), "Bruker");
	}

	private void assertRegistrering(no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering) {
		assertEquals(registrering.getJournalaar().toString(), "2020");
		assertEquals(registrering.getJournalsekvensnummer().toString(), "453637481");
		assertEquals(registrering.getJournalpostnummer().toString(), "453637481");
		assertEquals(registrering.getJournalposttype(), "Utgående dokument");
		assertEquals(registrering.getJournalstatus(), "Ferdig og klar for sentral utskrift");
		assertEquals(registrering.getJournaldato(), toLocalDateTime("2020-11-10 16:04:43").toLocalDate());
		assertEquals(registrering.getDokumentetsDato(), toLocalDateTime("2020-11-10 16:05:43").toLocalDate());
		assertThat(registrering.getSystemID().getValue()).isNotEmpty();
		assertThat(registrering.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals(registrering.getOpprettetAv(), "srvmelosys");
		assertEquals(registrering.getRegistreringsID(), "453637481");
		assertEquals(registrering.getTittel(), "Legg til ny institusjon");
		assertEquals(registrering.getKorrespondanseparts().size(), 1);
		assertEquals(registrering.getDokumentbeskrivelses().size(), 1);

		assertKorrespondanseparts(registrering.getKorrespondanseparts().get(0));
		assertDokumentBeskrivelse(registrering.getDokumentbeskrivelses().get(0));
	}

	private void assertDokumentObjekt(Dokumentobjekt dokObjekt) {
		assertFalse(dokObjekt.getSystemID().getValue().isEmpty());
		assertEquals(dokObjekt.getVersjonsnummer(), toBigInteger(1));
		assertEquals(dokObjekt.getVariantformat(), "Arkivformat");
		assertEquals(dokObjekt.getFormat(), "PDF");
		assertThat(dokObjekt.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals(dokObjekt.getOpprettetAv(), "Automatisk jobb");
		assertEquals(dokObjekt.getReferanseDokumentfil(), "DOKUMENTER/KTR/453637481_55c39cdb-f052-4f4e-a9a5-900b455ca915.pdf");
		assertEquals(dokObjekt.getSjekksum(), "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");
		assertEquals(dokObjekt.getSjekksumAlgoritme(), "SHA-256");
		assertEquals(dokObjekt.getFilstoerrelse(), toBigInteger(FIL.length()));
	}

	private void assertKorrespondanseparts(Korrespondansepart korrPart) {
		assertEquals(korrPart.getKorrespondanseparttype(), "Mottaker");
		assertEquals(korrPart.getKorrespondansepartNavn(), "Bruker Brukersen");
		assertEquals(korrPart.getSaksbehandler(), "srvmelosys");
	}

	private void assertDokumentBeskrivelse(Dokumentbeskrivelse dok) {
		assertFalse(dok.getSystemID().getValue().isEmpty());
		assertEquals(dok.getDokumenttype(), "Strukturert elektronisk dokument");
		assertEquals(dok.getDokumentstatus(), "Dokumentet er ferdigstilt");
		assertEquals(dok.getTittel(), "Legg til ny institusjon");
		assertThat(dok.getOpprettetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals(dok.getOpprettetAv(), "Automatisk jobb");
		assertEquals(dok.getTilknyttetRegistreringSom(), "HOVEDDOKUMENT");
		assertEquals(dok.getDokumentnummer(), toBigInteger(454017976));
		assertThat(dok.getTilknyttetDato()).isEqualToIgnoringNanos(toLocalDateTime("2020-11-10 16:04:43"));
		assertEquals(dok.getTilknyttetAv(), "Automatisk jobb");
		assertEquals(dok.getDokumentobjekts().size(), 1);

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
				.jp(List.of(generateJournalpost(UTGAAENDE))).build();
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