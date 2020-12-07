package no.nav.dokarkivavlevering.avlevering.consumer.activedirectory;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class NavActiveDirectoryConsumer {

	private final LdapTemplate ldapTemplate;
	private final AvleveringProperties.Activedirectory activedirectory;

	public NavActiveDirectoryConsumer(LdapTemplate ldapTemplate, AvleveringProperties avleveringProperties) {
		this.ldapTemplate = ldapTemplate;
		this.activedirectory = avleveringProperties.getActivedirectory();
	}

	public Map<String, String> hentNavAnsattBolk(final Set<String> adeoIdenter) {
		if(adeoIdenter.isEmpty()) {
			return new HashMap<>();
		}
		try {
			final List<NavAnsatt> navAnsatts = ldapTemplate.find(getQuery(adeoIdenter), NavAnsatt.class);
			return navAnsatts.stream().collect(Collectors.toMap(NavAnsatt::getUserId, NavAnsatt::getNavn));
		} catch(NamingException e) {
			log.error("Klarte ikke hente navn på NAV ansatte. Fortsetter behandling.", e);
			return new HashMap<>();
		}
	}

	private LdapQuery getQuery(final Set<String> adeoIdenter) {
		return query().base(activedirectory.getBasedn()).timeLimit(200)
				.filter(constructFilter(adeoIdenter));
	}

	private String constructFilter(final Set<String> adeoIdenter) {
		return "(|" + adeoIdenter.stream()
				.map(adeoIdent -> "(cn=" + adeoIdent + ")").collect(Collectors.joining()) + ")";
	}
}