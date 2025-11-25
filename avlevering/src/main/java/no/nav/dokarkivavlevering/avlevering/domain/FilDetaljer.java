package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class FilDetaljer implements MedOpprettetAv {
	Long id;
	String filUuid;
	LocalDateTime datoOpprettet;
	@ToString.Exclude
	String opprettetAv;
	@ToString.Exclude
	String opprettetAvBeriketNavn;
	byte[] fil;
	String filtype;
	int filstorrelseBeriket;
	String sha256hashBeriket;
}
