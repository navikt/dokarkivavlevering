package no.nav.dokarkivavlevering.avlevering;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.KlasseMapper;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.SaksmappeMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringSakService {
	private final SaksmappeMapper saksmappeMapper;

	public AvleveringSakService(SaksmappeMapper saksmappeMapper) {
		this.saksmappeMapper = saksmappeMapper;
	}

	@Handler
	public List<Saksmappe> avlevering(final List<Sak> saker, SystemID systemID) {
		// start forretningslogikk her
		// map til xmlstruktur
		// hent metadata
		// returner liste av xml objekter for marshal
		return saker.stream().map(sak -> {
			try {
				return saksmappeMapper.map(sak);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}//TODO: proper feilhåndtering (gjennom hele mapping-løpet)
			return null;
		})
				.collect(Collectors.toList());
	}
}
