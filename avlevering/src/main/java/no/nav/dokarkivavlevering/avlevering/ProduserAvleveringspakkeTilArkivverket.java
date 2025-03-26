package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.exception.MissingPropertiesException;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
public class ProduserAvleveringspakkeTilArkivverket {

	private final ProducerTemplate producerTemplate;
	private final DokarkivavleveringProperties dokarkivavleveringProperties;

	public ProduserAvleveringspakkeTilArkivverket(ProducerTemplate producerTemplate, DokarkivavleveringProperties dokarkivavleveringProperties) {
		this.producerTemplate = producerTemplate;
		this.dokarkivavleveringProperties = dokarkivavleveringProperties;
	}

	public void execute() {
		validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt();
		producerTemplate.send("direct:start_intermediate", exchange -> exchange.getIn().setBody("Start avlevering"));
	}


	private void validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt() {
		if (isNull(dokarkivavleveringProperties.getPeriode().getStartdato())) {
			throw new MissingPropertiesException("Startdato er null");
		}
		if (isNull(dokarkivavleveringProperties.getPeriode().getSluttdato())) {
			throw new MissingPropertiesException("Sluttdato er null");
		}
		if (isNull(dokarkivavleveringProperties.getTema())) {
			throw new MissingPropertiesException("Tema er null");
		}
	}
}