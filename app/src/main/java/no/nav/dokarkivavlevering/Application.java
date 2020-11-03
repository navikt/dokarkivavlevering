package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
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

    @Value("${dokarkivavlevering.job.startdato}")
    private String startDato;

    @Value("${dokarkivavlevering.job.sluttdato}")
    private String sluttDato;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Dokarkivavlevering avslutter...");
        context.close();
    }

    @Override
    public void run(String... args) {
        log.info("Dokarkivavlevering starter...");
        log.info("Startdato: " + startDato);
        log.info("Sluttdato: " + sluttDato);
    }
}
