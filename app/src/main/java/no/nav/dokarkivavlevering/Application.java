package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;


@Slf4j
@Import(value= {AppConfig.class})
@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Dokarkivavlevering avslutter...");
        context.close();
    }

    @Override
    public void run(String... args) {
        log.info("Dokarkivavlevering kjører...");
        for(String arg:args) {
            log.info(arg);
        }
    }
}
