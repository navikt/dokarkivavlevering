package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DatavarehusResponse {

	List<AdministrativEnhet> items;

	public record AdministrativEnhet(
			@JsonProperty("mapping_node_type")
			String kontortype,
			@JsonProperty("nav_enhet_navn")
			String kontornavn
	){

	}

}
