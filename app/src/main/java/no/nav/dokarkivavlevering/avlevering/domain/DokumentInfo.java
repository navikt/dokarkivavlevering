package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class DokumentInfo {

	UUID uuid = UUID.randomUUID();
	Long id;
	String relTilknyttetSom;
	LocalDateTime relDatoOpprettet;
	@ToString.Exclude
	String relOpprettetAv;
	@ToString.Exclude
	String relOpprettetAvBeriketNavn;
	String kategoriDecode;
	String status;
	@ToString.Exclude
	String tittel;
	LocalDateTime datoOpprettet;
	@ToString.Exclude
	String opprettetAv;
	LocalDateTime datoFerdig;
	@ToString.Exclude
	String opprettetAvBeriketNavn;
	List<FilDetaljer> fd;
	List<Arkivendring> ae;

	public String getOpprettetAv() {
		return opprettetAv;
	}
}
