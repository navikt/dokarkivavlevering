package no.nav.dokarkivavlevering.avlevering.consumer.activedirectory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
@Profile("genererAvlevering")
public class LdapConfig {
	@Bean
	LdapTemplate ldapTemplate(ContextSource contextSource) {
		return new LdapTemplate(contextSource);
	}
}