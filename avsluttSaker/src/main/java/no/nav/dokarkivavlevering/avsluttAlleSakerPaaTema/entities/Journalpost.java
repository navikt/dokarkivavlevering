package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.Immutable;

import java.util.Date;

@Entity
@Immutable
@Table(name = "T_JOURNALPOST")
public class Journalpost {

	@Id
	@Column(name = "journalpost_id")
	private Long journalpostId;

	@Column(name = "k_journal_s")
	private String journalstatus;

	@Column
	private boolean erFeilregistrert; // eksisterer ikkje

	@Column(name = "journalf_enhet")
	private String journalfoerendeEnhet;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_journal")
	private Date journaldato;

	// Bidireksjonelle OneToOne relasjoner blir eager fetched fra Journalpost
	@OneToOne(mappedBy = "journalpost")
	private Saksrelasjon saksrelasjon;

}