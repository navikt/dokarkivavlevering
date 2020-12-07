package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
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
				for (Arkivendring ae : jp.getAe()) {
					if (ae.getTidspunkt().after(jp.getDatoJournal())) {
						endringer.add(endringsloggMapper.map(ae, ae.getElement().startsWith(SAKSRELASJON) ? sak.getUuid() : jp.getUuid()));
					}
				}
				for (DokumentInfo di : jp.getDok()) {
					for (Arkivendring ae : di.getAe()) {
						if (ae.getTidspunkt().after(jp.getDatoJournal())) {
							endringer.add(endringsloggMapper.map(ae, di.getUuid()));
						}
					}
				}
			}
		}
		return endringer;
	}
}