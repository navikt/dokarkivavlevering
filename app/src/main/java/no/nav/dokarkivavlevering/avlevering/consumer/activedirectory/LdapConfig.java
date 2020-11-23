package no.nav.dokarkivavlevering.avlevering.consumer.activedirectory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
public class LdapConfig {
	@Bean
	LdapTemplate ldapTemplate(ContextSource contextSource) {
		return new LdapTemplate(contextSource);
	}
}