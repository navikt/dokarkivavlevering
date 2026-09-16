package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDate;

@Value
@AllArgsConstructor
public class StartSluttDato {

	LocalDate startdato;
	LocalDate sluttdato;

}
