package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.Arbeidsstatus;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AVSLUTTSAKER_TMP_TABELL")
public class Arbeidssak {

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

	@Column(name = "k_sak_status")
	String status;

	@Column
	Arbeidsstatus arbeidsstatus;
}