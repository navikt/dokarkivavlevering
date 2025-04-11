package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions;

public class KanIkkeBehandleArkivsakException extends RuntimeException {
	public KanIkkeBehandleArkivsakException(String message) {
		super(message);
	}
}
