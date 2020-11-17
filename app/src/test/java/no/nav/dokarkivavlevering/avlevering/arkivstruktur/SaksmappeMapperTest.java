package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
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
		final Saksmappe saksmappe = saksmappeMapper.map(Sak.builder().build(), sakSystemID);
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
	}
}