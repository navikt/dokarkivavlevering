package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import lombok.Data;

@Data
public class EregHentNoekkelInfoResponse {
	private Navn navn;

	@Data
	public static class Navn {
		private final String navnelinje1;
		private final String navnelinje2;
		private final String navnelinje3;
		private final String navnelinje4;
		private final String navnelinje5;
	}
}
