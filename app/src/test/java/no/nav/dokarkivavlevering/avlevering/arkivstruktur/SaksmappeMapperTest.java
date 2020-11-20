package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class SaksmappeMapperTest {

	private enum journalpostType {
		U, I, N
	}

	private enum journalpostStatus {
		FS, J, FL
	}

	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper();

	@Test
	void shouldMap() throws DatatypeConfigurationException {
		SystemID sakSystemID = new SystemID();
		sakSystemID.setValue(UUID.randomUUID().toString());

		Journalpost jp = generateJournalPost();
		DokumentInfo dokInfo = generateDokumentInfo();
		dokInfo.getFildetaljer().add(generateFilDetaljer());
		jp.getDokumenter().add(dokInfo);
		Sak sak = generateSak();
		sak.getJournalposter().add(jp);

		final Saksmappe saksmappe = saksmappeMapper.map(sak);
		//assertThat(saksmappe.)
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
	}

	private Sak generateSak() {
		return Sak.builder()
				.id((long) 1234567011)
				.tema("MED")
				.bruker(new Bruker("12009988772"))
				.opprettetAv("Skrue McDuck")
				.opprettetTidspunkt(new Date(2018, 9, 24))
				.journalposter(new ArrayList<Journalpost>()).build();

	}


	private Journalpost generateJournalPost() {

		return new Journalpost(
				(long) 88888888,
				journalpostType.U.toString(),
				journalpostStatus.J.toString(),
				"Søknad om arbeidsavklaringspenger",
				"Max Mekker",
				new Date(2018, 10, 24),
				new Date(2018, 10, 24),
				new Date(2018, 10, 24),
				new Date(2018, 10, 24),
				new Date(2018, 10, 24),
				new Date(2018, 10, 24),
				"BJOARK002",
				"BJOARK002",
				"Automatisk jobb",
				new ArrayList<DokumentInfo>(),
				new ArrayList<Arkivendring>());
	}

	private DokumentInfo generateDokumentInfo() {
		return new DokumentInfo((long) 987654321,
				"hæ",
				new Date(2018, 11, 24),
				"Tenke Tenkesen",
				"SED",
				"FERDIGSTILT",
				"dokumentTittel",
				new Date(2018, 10, 24),
				"Donald Duck",
				new ArrayList<FilDetaljer>(),
				new ArrayList<Arkivendring>());

	}

	private FilDetaljer generateFilDetaljer() {
		return new FilDetaljer((long) 539711281, "607971e3-349f-460b-823c-24ef873cfafe", new Date(2018, 10, 24), "srvRuting");
	}
}