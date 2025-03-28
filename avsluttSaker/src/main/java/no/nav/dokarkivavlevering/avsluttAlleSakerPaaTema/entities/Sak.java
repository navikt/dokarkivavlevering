package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "toBeDetermined")
public class Sak {

	@Id
	@Column(name = "id", nullable = false)
	Long sakId;

	@Column(nullable = false)
	String applikasjon;

	@Column(nullable = false)
	String fagsaknr;

	@Column(nullable = false)
	String aktoerId;

	@Column(nullable = false)
	String orgnr;

	@Column
	String status;
}
