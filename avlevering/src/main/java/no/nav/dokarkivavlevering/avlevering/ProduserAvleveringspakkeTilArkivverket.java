package no.nav.dokarkivavlevering.avlevering;

import org.apache.camel.ProducerTemplate;

public class ProduserAvleveringspakkeTilArkivverket {

	private final ProducerTemplate producerTemplate;

	public ProduserAvleveringspakkeTilArkivverket(ProducerTemplate producerTemplate) {
		this.producerTemplate = producerTemplate;
	}

	public void execute() {
		producerTemplate.send("direct:start_intermediate", exchange -> exchange.getIn().setBody("Start avlevering"));
	}
}