package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.ProduserAvleveringspakkeTilArkivverket;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.time.LocalDateTime;

@Slf4j
@Command(group = "arkiv")
public class Commands {

	private final ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket;

	public Commands(ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket) {
		this.produserAvleveringspakkeTilArkivverket = produserAvleveringspakkeTilArkivverket;
	}

	@Command(command = "produser-avleveringspakke-til-arkivverket",
			description = "Produser avleveringspakke til Arkivverket.")
	public void produserAvleveringspakkeTilArkivverket(@Option(required = true) LocalDateTime periodeStart,
													   @Option(required = true) LocalDateTime periodeSlutt,
													   @Option(required = true) String tema) {
		log.info("produserAvleveringspakkeTilArkivverket skal produsere avleveringspakke til Arkivverket for tema={} med periodeStart={} og periodeSlutt={}",
				tema, periodeStart, periodeSlutt);

		produserAvleveringspakkeTilArkivverket.execute();
	}

}