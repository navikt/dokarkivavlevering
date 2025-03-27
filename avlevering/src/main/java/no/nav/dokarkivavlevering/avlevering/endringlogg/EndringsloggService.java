package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("genererAvlevering")
public class EndringsloggService {

	private static final String SAKSRELASJON = "Saksrelasjon";

	private final EndringsloggMapper endringsloggMapper;

	public EndringsloggService(EndringsloggMapper endringsloggMapper) {
		this.endringsloggMapper = endringsloggMapper;
	}

	@Handler
	public List<Endring> avlevering(@Body final List<Sak> saker) {

		List<Endring> endringer = new ArrayList<>();
		for (Sak sak : saker) {
			for (Journalpost jp : sak.getJp()) {
				jp.getAe()
						.stream()
						.filter(ae -> ae.getTidspunkt().isAfter(jp.getDatoJournal()))
						.map(ae -> endringsloggMapper.map(ae, determineCorrectUUID(ae, sak, jp)))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.forEach(endringer::add);

				for (DokumentInfo dokumentInfo : jp.getDok()) {
					dokumentInfo.getAe()
							.stream()
							.filter(ae -> ae.getTidspunkt().isAfter(jp.getDatoJournal()))
							.map(ae -> endringsloggMapper.map(ae, dokumentInfo.getUuid()))
							.filter(Optional::isPresent)
							.map(Optional::get)
							.forEach(endringer::add);
				}
			}
		}
		return endringer;
	}

	private static UUID determineCorrectUUID(Arkivendring ae, Sak sak, Journalpost jp) {
		if (ae.getElement().startsWith(SAKSRELASJON)) {
			return sak.getUuid();
		} else {
			return jp.getUuid();
		}
	}
}