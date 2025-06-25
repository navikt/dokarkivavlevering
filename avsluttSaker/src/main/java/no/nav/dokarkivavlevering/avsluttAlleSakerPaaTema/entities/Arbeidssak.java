package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;

import static jakarta.persistence.EnumType.STRING;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MMA_8141")
public class Arbeidssak {

	@Id
	@Column(name = "id", nullable = false)
	Long sakId;

	@Column(nullable = false)
	String applikasjon;

	@Column
	String fagsaknr;

	@Column(name = "aktoerid")
	String aktoerId;

	@Column
	String orgnr;

	@Column(name = "k_sak_status")
	String status;

	@Column
	@Enumerated(STRING)
	Arbeidsstatus arbeidsstatus;
}