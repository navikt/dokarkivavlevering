package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class Fagomrade {

	String fagomrade;
	String dekode;
	LocalDate datoFom;
	LocalDate datoTom;
	LocalDateTime datoOpprettet;
	String opprettetAv;
	LocalDateTime datoEndret;
	String endretAv;

	// Oracle SQL to Java compatibility shim - convert String to boolean
	@Getter(AccessLevel.NONE)
	String erGyldig;

	public boolean erGyldigAkkuratNaa() {
		return "1".equalsIgnoreCase(erGyldig) && (datoTom == null || datoTom.isAfter(LocalDate.now()));
	}

}
