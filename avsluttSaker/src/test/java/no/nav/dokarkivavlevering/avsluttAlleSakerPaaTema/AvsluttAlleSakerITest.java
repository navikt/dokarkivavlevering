package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.config.AbstractITest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AvsluttAlleSakerITest extends AbstractITest {

//	@Autowired
//	AvsluttAlleSakerPaaTema avsluttAlleSakerPaaTema;

	@Test
	public void shouldStart() {
		stubPdl("hentIdenterBolk.json");



		System.out.println("test");
	}

	@Test
	public void skal() {
		stubPdl("validationError.json");
		populerSakRepository();

//		avsluttAlleSakerPaaTema.execute();

		System.out.println("test");
	}

	void populerSakRepository() {
		Sak sakForPerson1 = new Sak().builder()
				.sakId(123L)
				.applikasjon("FS22")
				.fagsaknr(null)
				.aktoerId("12345678911")
				.orgnr(null)
				.build();

		Sak sakForPerson2 = new Sak().builder()
				.sakId(234L)
				.applikasjon("AO01")
				.fagsaknr("123")
				.aktoerId("12345678912")
				.orgnr(null)
				.build();

		Sak sakForOrganisasjon = new Sak().builder()
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

	protected static void stubPdl(String filename) {
		stubFor(post("/pdl")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}
}
