package no.nav.dokarkivavlevering.avlevering.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Journalpost implements MedOpprettetAvNavn {
	UUID uuid = UUID.randomUUID();
	Long id;
	String type;
	String status;
	@ToString.Exclude
	String innhold;
	@ToString.Exclude
	String avsenderMottaker;
	String avsenderMottakerId;
	String avsenderMottakerIdType;
	String offentligJournalAvsenderMottaker;
	LocalDateTime datoMottatt;
	LocalDateTime datoDokument;
	LocalDateTime datoJournal;
	LocalDateTime datoOpprettet;
	LocalDateTime datoEndret;
	LocalDateTime datoEkspedert;
	LocalDateTime datoSendtPrint;
	@ToString.Exclude
	String opprettetAv;
	@ToString.Exclude
	String opprettetAvBeriketNavn;
	@ToString.Exclude
	String opprettetAvNavn;
	@ToString.Exclude
	String endretAv;
	@ToString.Exclude
	String endretAvBeriketNavn;
	List<DokumentInfo> dok;
	List<Arkivendring> ae;

	public String getHoveddokumentTittel() {
		final Optional<DokumentInfo> hoveddokument = dok.stream()
				.filter(d -> "HOVEDDOKUMENT".equals(d.getRelTilknyttetSom()))
				.findFirst();
		return hoveddokument.map(DokumentInfo::getTittel).orElse(null);
	}
}
