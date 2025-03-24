package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.ProduserAvleveringspakkeTilArkivverket;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.shell.command.annotation.Command;

import static java.util.Objects.isNull;

@Slf4j
@Command(group = "arkiv")
public class Commands {

	private final ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket;
	private final DokarkivavleveringProperties dokarkivavleveringProperties;

	public Commands(ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket, DokarkivavleveringProperties dokarkivavleveringProperties) {
		this.produserAvleveringspakkeTilArkivverket = produserAvleveringspakkeTilArkivverket;
		this.dokarkivavleveringProperties = dokarkivavleveringProperties;
	}

	@Command(command = "produser-avleveringspakke-til-arkivverket",
			description = "Produser avleveringspakke til Arkivverket.")
	public void produserAvleveringspakkeTilArkivverket() {
		validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt();
		log.info("produserAvleveringspakkeTilArkivverket skal produsere avleveringspakke til Arkivverket for tema={} med periodeStart={} og periodeSlutt={}",
				dokarkivavleveringProperties.getTema(), dokarkivavleveringProperties.getPeriode().getStartdato(), dokarkivavleveringProperties.getPeriode().getSluttdato());
		produserAvleveringspakkeTilArkivverket.execute();
	}

	private void validerPåkrevdeProduserAvleveringspakkeTilArkivverketPropertiesErSatt(){
		if(isNull(dokarkivavleveringProperties.getPeriode().getStartdato())){
			throw new MissingPropertiesException("Startdato er null");
		}
		if(isNull(dokarkivavleveringProperties.getPeriode().getSluttdato())){
			throw new MissingPropertiesException("Sluttdato er null");
		}
		if(isNull(dokarkivavleveringProperties.getTema())){
			throw new MissingPropertiesException("Tema er null");
		}
	}
}