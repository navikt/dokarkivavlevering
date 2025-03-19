package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Arkivendring {
	public static final String INGEN_VERDI = "ingen verdi";
	Long id;
	String element;
	LocalDateTime tidspunkt;
	@ToString.Exclude
	String utfoertAv;
	@ToString.Exclude
	String utfoertAvBeriketNavn;
	String fraVerdi;
	String tilVerdi;
}
