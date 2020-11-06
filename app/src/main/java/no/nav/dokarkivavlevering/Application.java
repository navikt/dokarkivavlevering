package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.config.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;


@Slf4j
@Import(value = {AppConfig.class})
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Value("${periode.startdato}")
    private String startDato;

    @Value("${periode.sluttdato}")
    private String sluttDato;

    @Autowired
    private ServiceuserAlias serviceuserAlias;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Dokarkivavlevering avslutter...");
        context.close();
    }

    @Override
    public void run(String... args) {
        // Test av at applikasjon tar inn inputvariabler og serviceuser
        log.info("Dokarkivavlevering starter...");
        log.info("Startdato: " + startDato + ", Sluttdato: " + sluttDato);
        log.info("serviceuser-test: " + serviceuserAlias.getUsername());
    }
}
