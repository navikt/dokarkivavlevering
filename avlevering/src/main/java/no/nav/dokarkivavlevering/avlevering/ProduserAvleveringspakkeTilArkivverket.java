package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
@Profile("genererAvlevering")
public class ProduserAvleveringspakkeTilArkivverket {

	private final ProducerTemplate producerTemplate;
	private final AvleveringProperties avleveringProperties;

	public ProduserAvleveringspakkeTilArkivverket(ProducerTemplate producerTemplate, AvleveringProperties avleveringProperties) {
		this.producerTemplate = producerTemplate;
		this.avleveringProperties = avleveringProperties;
	}

	@Scheduled(initialDelay = 1000)
	public void execute() {
		log.info("produserAvleveringspakkeTilArkivverket skal produsere avleveringspakke til Arkivverket for tema={} med periodeStart={} og periodeSlutt={}",
				avleveringProperties.getTema(), avleveringProperties.getPeriode().getStartdato(), avleveringProperties.getPeriode().getSluttdato());
		validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt();
		producerTemplate.send("direct:start_intermediate", exchange -> exchange.getIn().setBody("Start avlevering"));
	}


	private void validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt() {
		if (isNull(avleveringProperties.getPeriode().getStartdato())) {
			throw new MissingPropertiesException("Startdato er null");
		}
		if (isNull(avleveringProperties.getPeriode().getSluttdato())) {
			throw new MissingPropertiesException("Sluttdato er null");
		}
		if (isNull(avleveringProperties.getTema())) {
			throw new MissingPropertiesException("Tema er null");
		}
	}
}