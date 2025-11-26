package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.Data;
import lombok.ToString;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PdlHentPersonBolkResponse {
	public static final String CODE_OK = "ok";
	private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");
	private PdlHentIdenterBolkData data;
	private List<PdlError> errors;

	@Data
	public static class PdlHentIdenterBolkData {
		private List<PdlHentPersonBolk> hentPersonBolk;
	}

	@Data
	public static class PdlHentPersonBolk {
		@ToString.Exclude
		private String ident;
		private String code;
		private PdlPerson person;
	}

	@Data
	public static class PdlPerson {
		private List<PdlFolkeregisteridentifikator> folkeregisteridentifikator;
		private List<PdlNavn> navn;
	}

	@Data
	public static class PdlNavn {
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
	}

	@Data
	public static class PdlFolkeregisteridentifikator {
		@ToString.Exclude
		private String identifikasjonsnummer;
	}

	@Data
	public static class PdlFolkeregistermetadata {
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
		private PdlErrorExtensionTo extensions;
	}

	@Data
	static class PdlErrorExtensionTo {
		private String code;
		private String classification;
	}
}
