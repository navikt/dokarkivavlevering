package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.config.AbstractITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AvsluttAlleSakerITest extends AbstractITest {

	@Autowired
	AvsluttAlleSakerService avsluttAlleSakerService;

	@Test
	public void shouldStart() {
		stubPdl("hentIdenterBolk.json");


		System.out.println("test");
	}

	@Test
	public void skal() {
		stubPdl("hentIdenterBolk.json");
		setupHappyPathAzureToken();
		populerSakRepository();

		List<Long> sakIds = sakRepository.findAllSakIds();

		assertThat(sakIds).isEqualTo(List.of(123L, 234L, 345L));

		avsluttAlleSakerService.avsluttAlleSaker();
		commitAndBeginNewTransaction();
		List<Sak> sak1 = sakRepository.findSaksBySakIdIn(List.of(123L));
		assertThat(sak1.getFirst().getStatus()).isEqualTo("HENTET_FRA_PDL");
		System.out.println("test");
	}

	void populerSakRepository() {
		Sak sakForPerson1 = Sak.builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		Sak sakForPerson2 = Sak.builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		Sak sakForOrganisasjon = Sak.builder()
				.sakId(345L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId(null)
				.orgnr("123456789")
				.build();

		sakRepository.saveAll(List.of(sakForPerson1, sakForPerson2, sakForOrganisasjon));
		commitAndBeginNewTransaction();
	}


	protected static void stubPdl() {
		stubPdl("hentIdenterBolk.json");
	}

	protected static void setupHappyPathAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected static void stubPdl(String filename) {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}
}
