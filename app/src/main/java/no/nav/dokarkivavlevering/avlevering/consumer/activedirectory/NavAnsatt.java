package no.nav.dokarkivavlevering.avlevering.consumer.activedirectory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Representerer en NAV Ansatt fra active directory
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"displayName", "description"})
@Entry(objectClasses = {"organizationalPerson"})
public final class NavAnsatt {
	@Id
	private Name dn;

	@Attribute(name = "cn")
	private String userId;
	private String displayName;
	private String description;

	public String getNavn() {
		if (isNotBlank(description)) {
			return description;
		}
		if (isNotBlank(displayName)) {
			return displayName;
		}
		//fallback
		return userId;
	}
}



