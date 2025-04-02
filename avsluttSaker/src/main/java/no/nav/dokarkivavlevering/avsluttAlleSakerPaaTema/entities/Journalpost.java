package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "T_JOURNALPOST")
public class Journalpost {

	@Id
	@Column(name = "journalpost_id", nullable = false)
	private Long journalpostId;
	@Column
	private String journalstatus;
	@Column
	private boolean erFeilregistrert;
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private String journalførendeEnhet;
	@Column
	private Date datoJournal;


}
