package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusConsumer;

public class DvhService {

	private DatavarehusConsumer datavarehusConsumer;

	public DvhService(DatavarehusConsumer datavarehusConsumer) {
		this.datavarehusConsumer = datavarehusConsumer;
	}

	public void hentAdministrativeEnheter() {
		datavarehusConsumer.hentAlleAdministrativeEnheter();
	}
}
