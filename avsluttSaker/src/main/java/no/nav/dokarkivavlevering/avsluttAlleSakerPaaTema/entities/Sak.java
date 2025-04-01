package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TO_BE_DETERMINED")
public class Sak {

	@Id
	@Column(name = "id", nullable = false)
	Long sakId;

	@Column(nullable = false)
	String applikasjon;

	@Column
	String fagsaknr;

	@Column
	String aktoerId;

	@Column
	String orgnr;

	@Column
	String status;

}