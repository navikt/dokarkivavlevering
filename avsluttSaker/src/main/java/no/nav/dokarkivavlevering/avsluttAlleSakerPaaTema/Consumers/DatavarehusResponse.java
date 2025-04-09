package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatavarehusResponse {

	List<AdministrativEnhet> items;
	boolean hasMore;
	int limit;
	int offset;
	int count;
	List<String> links;

	public record AdministrativEnhet(
			@JsonProperty("mapping_node_type")
			String kontortype,
			@JsonProperty("nav_enhet_navn")
			String kontornavn
	){

	}

}
