package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class SaksmappeMapperTest {

	private final SaksmappeMapper saksmappeMapper = new SaksmappeMapper();

	@Test
	void shouldMap() {
		final Saksmappe saksmappe = saksmappeMapper.map(Sak.builder().build());
		assertThat(saksmappe.getSystemID().getValue()).isNotEmpty();
	}
}