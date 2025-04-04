package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_SAKSRELASJON")
public class Saksrelasjon {

	@Id
	@Column(name = "saksrelasjon_id")
	private Long saksrelasjonId;

	@Column(name = "sak_id")
	private Long sakId;

	@Column(name = "journalpost_id")
	private Long journalpostId;

	@Column(name = "feilregistrert")
	private Boolean feilregistrert;

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

}