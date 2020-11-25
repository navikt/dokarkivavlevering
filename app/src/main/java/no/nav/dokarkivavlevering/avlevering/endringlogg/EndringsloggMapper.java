package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils.Utils.dateToXMLGregorianCalendar;

@Component
public class EndringsloggMapper {

	public Endring map(Arkivendring arkivendring, UUID uuid) {
		Endring endring = new Endring();
		endring.setReferanseArkivenhet(uuid.toString());
		endring.setReferanseMetadata(arkivendring.getElement());
		endring.setEndretDato(dateToXMLGregorianCalendar(arkivendring.getTidspunkt()));
		endring.setEndretAv(arkivendring.getUtfoertAv());
		endring.setTidligereVerdi(arkivendring.getFraVerdi());
		endring.setNyVerdi(arkivendring.getTilVerdi());
		return endring;
	}
}
