package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivstruktur.Part;
import no.arkivverket.standarder.noark5.arkivstruktur.Registrering;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.common.JournaldatoMapper;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.formatter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */

class SaksmappeMapperTest {

	public static final String FIL = "Hello World";
	private final AvleveringProperties avleveringProperties = new AvleveringProperties();
	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper(new JournaldatoMapper(), avleveringProperties);

	@Test
	void shouldMap() throws Exception {
		SystemID sakSystemID = new SystemID();
		sakSystemID.setValue(UUID.randomUUID().toString());

		Sak sak = generateSak();

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		//saksmappe
		assertEquals(saksmappe.getSaksaar().toString(), "2019");
		assertEquals(saksmappe.getSakssekvensnummer().toString(), "1234567011");
		assertEquals(saksmappe.getSaksdato(), TestUtils.toXmlGregCalendar("2019-10-28 11:41:36"));
		assertEquals(saksmappe.getAdministrativEnhet(), "NAV Kontroll");
		assertEquals(saksmappe.getSaksansvarlig(), "Bjarne Betjent");
		assertEquals(saksmappe.getSaksstatus(), "Under behandling");
		assertEquals(saksmappe.getSystemID().getValue().isEmpty(), false);
		assertEquals(saksmappe.getMappeID(), "1234567011");
		assertEquals(saksmappe.getTittel(), "Kontroll");
		assertEquals(saksmappe.getOpprettetDato(), TestUtils.toXmlGregCalendar("2019-10-28 11:41:36"));
		assertEquals(saksmappe.getOpprettetAv(), "Automatisk jobb");
		assertEquals(saksmappe.getReferanseArkivdels().size(), 1);
		assertEquals(saksmappe.getParts().size(), 1);
		assertEquals(saksmappe.getRegistrerings().size(), 1);
		assertThat(saksmappe.getReferanseArkivdels()).contains(avleveringProperties.getArkivConfig().getArkivdelConfig().getSystemID());
		//saksmappe/part
		assertPart(saksmappe.getParts().get(0));

		assertRegistrering((no.arkivverket.standarder.noark5.arkivstruktur.Journalpost) saksmappe.getRegistrerings().get(0));
	}

	@Test
	void shouldMapOpprettetAvToBeriketNavnWhenOpprettetAvNavnIsNull() throws Exception {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost().toBuilder()
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
	void shouldMapOpprettetAvUkjentWhenOpprettetAvIsNull() throws Exception {
		final Sak sak = generateSak().toBuilder().jp(Collections.singletonList(
				generateJournalpost().toBuilder()
						.opprettetAv(null)
						.opprettetAvNavn(null)
						.opprettetAvBeriketNavn(null)
						.dok(Collections.singletonList(generateDokumentInfo()))
						.build())).build();
		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		final Registrering registrering = saksmappe.getRegistrerings().get(0);
		assertThat(registrering.getOpprettetAv()).isEqualTo(Bruker.UKJENT_PERSON);
	}

	private void assertPart(Part part) {
		assertEquals(part.getPartID(), "12345678911");
		assertEquals(part.getPartNavn(), "Frank");
		assertEquals(part.getPartRolle(), "Bruker");
	}

	private void assertRegistrering(no.arkivverket.standarder.noark5.arkivstruktur.Journalpost registrering) throws Exception {
		assertEquals(registrering.getJournalaar().toString(), "2020");
		assertEquals(registrering.getJournalsekvensnummer().toString(), "453637481");
		assertEquals(registrering.getJournalpostnummer().toString(), "453637481");
		assertEquals(registrering.getJournalposttype(), "Utgående dokument");
		assertEquals(registrering.getJournalstatus(), "Arkivert");
		assertEquals(registrering.getJournaldato(), TestUtils.toXmlGregCalendar("2020-11-10 16:04:43"));
		assertEquals(registrering.getDokumentetsDato(), TestUtils.toXmlGregCalendar("2020-11-10 16:05:43"));
		assertEquals(registrering.getSystemID().getValue().isEmpty(), false);
		assertEquals(registrering.getOpprettetDato(), TestUtils.toXmlGregCalendar("2020-11-10 16:04:43"));
		assertEquals(registrering.getOpprettetAv(), "srvmelosys");
		assertEquals(registrering.getRegistreringsID(), "453637481");
		assertEquals(registrering.getTittel(), "Legg til ny institusjon");
		assertEquals(registrering.getKorrespondanseparts().size(), 1);
		assertEquals(registrering.getDokumentbeskrivelses().size(), 1);

		assertKorrespondanseparts(registrering.getKorrespondanseparts().get(0));
		assertDokumentBeskrivelse(registrering.getDokumentbeskrivelses().get(0));
	}

	private void assertDokumentObjekt(Dokumentobjekt dokObjekt) throws Exception {
		assertEquals(dokObjekt.getSystemID().getValue().isEmpty(), false);
		assertEquals(dokObjekt.getVersjonsnummer(), toBigInteger(1));
		assertEquals(dokObjekt.getVariantformat(), "Arkivformat");
		assertEquals(dokObjekt.getFormat(), "PDF/A");
		assertEquals(dokObjekt.getOpprettetDato(), TestUtils.toXmlGregCalendar("2020-11-10 16:04:43"));
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

	private void assertDokumentBeskrivelse(Dokumentbeskrivelse dok) throws Exception {
		assertEquals(dok.getSystemID().getValue().isEmpty(), false);
		assertEquals(dok.getDokumenttype(), "OPP");
		assertEquals(dok.getDokumentstatus(), "Dokumentet er ferdigstilt");
		assertEquals(dok.getTittel(), "Legg til ny institusjon");
		assertEquals(dok.getOpprettetDato(), TestUtils.toXmlGregCalendar("2020-11-10 16:04:43"));
		assertEquals(dok.getOpprettetAv(), "Automatisk jobb");
		assertEquals(dok.getTilknyttetRegistreringSom(), "HOVEDDOKUMENT");
		assertEquals(dok.getDokumentnummer(), toBigInteger(454017976));
		assertEquals(dok.getTilknyttetDato(), TestUtils.toXmlGregCalendar("2020-11-10 16:04:43"));
		assertEquals(dok.getTilknyttetAv(), "Automatisk jobb");
		assertEquals(dok.getDokumentobjekts().size(), 1);

		assertDokumentObjekt(dok.getDokumentobjekts().get(0));
	}

	private BigInteger toBigInteger(int smallInteger) {
		return new BigInteger(String.valueOf(smallInteger));
	}

	private Sak generateSak() throws Exception {
		return Sak.builder()
				.id((long) 1234567011)
				.tema("KTR")
				.bruker(generaterBruker())
				.opprettetAv("srvmelosys")
				.opprettetTidspunkt(formatter.parse("2019-10-28 11:41:36.673"))
				.opprettetAvBeriketNavn("Automatisk jobb")
				.jp(Arrays.asList(generateJournalpost())).build();
	}

	private Bruker generaterBruker() {
		return new Bruker(
				"12345678911",
				"Frank"
		);
	}

	private Journalpost generateJournalpost() throws Exception {
		return Journalpost.builder()
				.id((long) 453637481)
				.type("U")
				.status("FS")
				.innhold("Legg til ny institusjon")
				.avsenderMottaker("Bruker Brukersen")
				.datoMottatt(null)
				.datoDokument(formatter.parse("2020-11-10 16:05:43.332"))
				.datoJournal(formatter.parse("2020-11-10 16:04:43.35"))
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.338"))
				.datoEkspedert(null)
				.datoSendtPrint(null)
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.opprettetAvNavn("srvmelosys")
				.endretAv("srvmelosys")
				.endretAvBeriketNavn("Bjarne Betjent")
				.dok(Arrays.asList(generateDokumentInfo()))
				.build();
	}

	private DokumentInfo generateDokumentInfo() throws Exception {
		return DokumentInfo.builder()
				.id((long) 454017976)
				.relTilknyttetSom("HOVEDDOKUMENT")
				.relDatoOpprettet(formatter.parse("2020-11-10 16:04:43.343"))
				.relOpprettetAv("srvmelosys")
				.relOpprettetAvBeriketNavn("Automatisk jobb")
				.kategori("OPP")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.fd(Arrays.asList(generateFilDetaljer()))
				.build();
	}

	private FilDetaljer generateFilDetaljer() throws Exception {
		return FilDetaljer.builder()
				.id((long) 539876247)
				.filUuid("55c39cdb-f052-4f4e-a9a5-900b455ca915")
				.fil(FIL.getBytes())
				.filstorrelseBeriket(FIL.length())
				.sha256hashBeriket("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e")
				.datoOpprettet(formatter.parse("2020-11-10 16:04:43.343"))
				.opprettetAv("srvRuting")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.build();
	}

}