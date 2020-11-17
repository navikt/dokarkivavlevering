package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SaksmappeMapper {
	public Saksmappe map(Sak sak) {
		Saksmappe mappe = new Saksmappe();
		SystemID sakSystemID = new SystemID();
		sakSystemID.setValue(UUID.randomUUID().toString());
		mappe.setSystemID(sakSystemID);
		return mappe;
	}
}
