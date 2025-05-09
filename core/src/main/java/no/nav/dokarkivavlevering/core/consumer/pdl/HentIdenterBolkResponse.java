package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class HentIdenterBolkResponse {
	private HentIdenterBolkData data;
	private List<Error> errors;

	@Data
	public static class HentIdenterBolkData {
		private List<HentIdenterBolk> hentIdenterBolk;
	}

	@Data
	public static class HentIdenterBolk {
		@ToString.Exclude
		//Ident vi sender inn
		private String ident;
		//ok / NOT_FOUND
		private String code;
		//Gjeldende ident
		private List<Ident> identer;
	}

	@Data
	public static class Ident {
		@ToString.Exclude
		private String ident;
	}

	@Data
	public static class Error {
		private String message;
		private ErrorExtension extensions;
	}

	@Data
	public static class ErrorExtension {
		private String code;
		private String classification;
	}
}