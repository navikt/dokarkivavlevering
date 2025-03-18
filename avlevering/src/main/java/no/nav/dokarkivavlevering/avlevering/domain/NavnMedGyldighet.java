package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class NavnMedGyldighet {
	ZonedDateTime gyldigFraOgMed;
	ZonedDateTime gyldigTil;
	String navn;

	boolean isValidFor(ZonedDateTime historiskTidspunkt) {
		boolean haddeBlittGyldig = gyldigFraOgMed != null && !gyldigFraOgMed.isAfter(historiskTidspunkt);
		boolean haddeIkkeOpphoert = gyldigTil == null || historiskTidspunkt.isBefore(gyldigTil);
		return haddeBlittGyldig && haddeIkkeOpphoert;
	}
}
