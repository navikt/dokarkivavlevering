package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "T_SAKSRELASJON_OLD")
public class saksrelasjon_old {

	@Id
	@Column(name = "saksrelasjon_id")
	private Long saksrelasjonId;

	@Column(name = "sak_id")
	private Long sakId;

	@Column(name = "journalpost_id")
	private Long journalpostId;

	@Column(name = "feilregistrert")
	private Boolean feilregistrert;

	/*
	@OneToOne
	@JsonIgnore
	@JoinColumn(name = "journalpost_id", nullable = false, insertable=false, updatable=false)
	private Journalpost journalpost;
	*/

}