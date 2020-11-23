package no.nav.dokarkivavlevering.avlevering.consumer.pdl;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class PdlHentPersonBolkResponse {
	private PdlHentIdenterBolkData data;
	private List<PdlError> errors;

	@Data
	static class PdlHentIdenterBolkData {
		private List<PdlHentPersonBolk> hentPersonBolk;
	}

	@Data
	static class PdlHentPersonBolk {
		@ToString.Exclude
		private String ident;
		private String code;
		private PdlPerson person;

		String getFolkeregisterIdent() {
			if ("ok".equals(code) && !person.getFolkeregisteridentifikator().isEmpty()) {
				return person.getFolkeregisteridentifikator().get(0).getIdentifikasjonsnummer();
			} else {
				return ident;
			}
		}

		String getFulltnavn() {
			if ("ok".equals(code) && !person.getNavn().isEmpty()) {
				PdlNavn pdlNavn = person.getNavn().get(0);
				return pdlNavn.getFornavn() +
						(StringUtils.isBlank(pdlNavn.getMellomnavn()) ? "" : pdlNavn.getMellomnavn()) +
						pdlNavn.getEtternavn();
			} else {
				return "Ukjent person";
			}
		}
	}

	@Data
	static class PdlPerson {
		private List<PdlFolkeregisteridentifikator> folkeregisteridentifikator;
		private List<PdlNavn> navn;
	}

	@Data
	static class PdlNavn {
		@ToString.Exclude
		private String fornavn;
		@ToString.Exclude
		private String mellomnavn;
		@ToString.Exclude
		private String etternavn;
	}

	@Data
	static class PdlFolkeregisteridentifikator {
		@ToString.Exclude
		private String identifikasjonsnummer;
	}

	@Data
	static class PdlError {
		private String message;
		private PdlHentIdenterBolkResponse.PdlErrorExtensionTo extensions;
	}

	@Data
	static class PdlErrorExtensionTo {
		private String code;
		private String classification;
	}
}
