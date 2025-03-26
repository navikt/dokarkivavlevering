package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.ProduserAvleveringspakkeTilArkivverket;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DokarkivAvleveringApplicationStarter {

	private final ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket;
	private final DokarkivavleveringProperties dokarkivavleveringProperties;

	public DokarkivAvleveringApplicationStarter(ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket, DokarkivavleveringProperties dokarkivavleveringProperties) {
		this.produserAvleveringspakkeTilArkivverket = produserAvleveringspakkeTilArkivverket;
		this.dokarkivavleveringProperties = dokarkivavleveringProperties;
	}

	@Scheduled(initialDelay = 1000)
	public void startApplication(){
		switch(dokarkivavleveringProperties.getJobtype()){
			case AVLEVERING -> {
				log.info("produserAvleveringspakkeTilArkivverket skal produsere avleveringspakke til Arkivverket for tema={} med periodeStart={} og periodeSlutt={}",
						dokarkivavleveringProperties.getTema(), dokarkivavleveringProperties.getPeriode().getStartdato(), dokarkivavleveringProperties.getPeriode().getSluttdato());
				produserAvleveringspakkeTilArkivverket.execute();
			}
		}
	}
}
