package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class PdlHentIdenterBolkResponse {
	private PdlHentIdenterBolkData data;
	private List<PdlErrorTo> errors;

	@Data
	public static class PdlHentIdenterBolkData {
		private List<PdlHentIdenterBolk> hentIdenterBolk;
	}

	@Data
	public static class PdlHentIdenterBolk {
		@ToString.Exclude
		//Ident vi sender inn
		private String ident;
		//ok / NOT_FOUND
		private String code;
		//Gjeldende ident
		private List<PdlIdentTo> identer;
	}

	@Data
	public static class PdlIdentTo {
		@ToString.Exclude
		private String ident;
	}

	@Data
	public static class PdlErrorTo {
		private String message;
		private PdlErrorExtensionTo extensions;
	}

	@Data
	public static class PdlErrorExtensionTo {
		private String code;
		private String classification;
	}
}
