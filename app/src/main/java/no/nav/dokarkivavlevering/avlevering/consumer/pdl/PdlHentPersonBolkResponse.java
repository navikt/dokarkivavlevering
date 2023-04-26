package no.nav.dokarkivavlevering.avlevering.consumer.pdl;

import lombok.Data;
import lombok.ToString;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.NavnMedGyldighet;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PdlHentPersonBolkResponse {
	public static final String CODE_OK = "ok";
	private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");
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
			if (CODE_OK.equals(code) && !person.getFolkeregisteridentifikator().isEmpty()) {
				return person.getFolkeregisteridentifikator().get(0).getIdentifikasjonsnummer();
			} else {
				return ident;
			}
		}

		String getFulltnavn() {
			if (CODE_OK.equals(code)) {
				return person.getNavn().stream()
						.filter(navn -> !navn.getMetadata().historisk)
						.map(PdlNavn::fulltnavn)
						.findFirst()
						.orElse(Bruker.UKJENT_PERSON);
			} else {
				return Bruker.UKJENT_PERSON;
			}
		}

		public List<NavnMedGyldighet> getNavnMedGyldighet() {
			return person.getNavn().stream()
					.map(PdlNavn::toNavnMedGyldighet)
					.toList();
		}

		public BrukerMedNavnedata toBrukerMedNavnedata() {
			return new BrukerMedNavnedata(getIdent(), getNavnMedGyldighet());
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
        private PdlFolkeregistermetadata pdlFolkeregistermetadata;
		private PdlNavnMetadata metadata;

		String fulltnavn() {
			return Stream.of(fornavn, mellomnavn, etternavn)
					.filter(Objects::nonNull)
					.collect(Collectors.joining(" "));
		}

		private static ZonedDateTime parseZonedDateTime(String tidspunkt) {
			return LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(tidspunkt)).atZone(OSLO);
		}

		private NavnMedGyldighet toNavnMedGyldighet() {
			return new NavnMedGyldighet(parseZonedDateTime(getPdlFolkeregistermetadata().gyldighetstidspunkt), parseZonedDateTime(getPdlFolkeregistermetadata().gyldighetstidspunkt), fulltnavn());
		}
	}

	@Data
	static class PdlFolkeregisteridentifikator {
		@ToString.Exclude
		private String identifikasjonsnummer;
	}

	@Data
	static class PdlFolkeregistermetadata {
		private String gyldighetstidspunkt;
		private String opphoerstidspunkt;
		private int sekvens;
	}

	@Data
	static class PdlNavnMetadata {
		private String opplysningsId;
		private boolean historisk;
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
