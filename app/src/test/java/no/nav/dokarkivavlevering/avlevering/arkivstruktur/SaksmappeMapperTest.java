package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import lombok.ToString;
import no.arkivverket.standarder.noark5.arkivstruktur.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class SaksmappeMapperTest {

	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper();

	@Test
	void shouldMap() throws DatatypeConfigurationException {
		SystemID sakSystemID = new SystemID();
		sakSystemID.setValue(UUID.randomUUID().toString());
		final Saksmappe saksmappe = saksmappeMapper.map(Sak.builder().build());
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
	}

	@Test
	void  generateDokumentInfo(){
		Date relasjondatoOpprettet = new Date(2018, 11,24 );
		Date datoOpprettet = new Date(2018, 10, 24);
		return new DokumentInfo((long)987654321,
				"hæ",
				relasjondatoOpprettet,
				"Tenke Tenkesen",
				"SED",
				"FERDIGSTILT",
				"dokumentTittel",
				datoOpprettet,
				"Donald Duck",
				)
		/*
		private final Long id;
		private final String relasjonTilknyttetSom;
		private final Date relasjonDatoOpprettet;
		@ToString.Exclude
		private final String relasjonOpprettetAv;
		private final String kategori;
		private final String status;
		@ToString.Exclude
		private final String tittel;
		private final Date datoOpprettet;
		@ToString.Exclude
		private final String opprettetAv;
		private final List<FilDetaljer> fildetaljer;
		private final List<Arkivendring> arkivendringer;*/
	}

	private FilDetaljer generateFilDetaljer(){
		Date datoOpprettet = new Date("2020-05-19 16:15:11.234000");
		return new FilDetaljer((long)1234567891, "abc-For-En-Fil-UUid!", datoOpprettet, "Her må jeg tenke litt");
	}
}