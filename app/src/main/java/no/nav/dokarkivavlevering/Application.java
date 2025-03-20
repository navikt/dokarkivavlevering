package no.nav.dokarkivavlevering;

import no.nav.dokarkivavlevering.core.CoreConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import(value = {CoreConfig.class})
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}
}