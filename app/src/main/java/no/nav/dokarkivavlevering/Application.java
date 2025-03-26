package no.nav.dokarkivavlevering;

import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.azure.AzureProperties;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DokarkivavleveringProperties.class, AzureProperties.class})
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}
}