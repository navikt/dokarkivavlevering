package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.arkivuttrekk.AvleveringArkivuttrekkRoute;
import no.nav.dokarkivavlevering.avlevering.aspose.AsposeService;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.endringlogg.AvleveringEndringsloggRoute;
import no.nav.dokarkivavlevering.avlevering.loependejournal.AvleveringLoependeJournalRoute;
import no.nav.dokarkivavlevering.avlevering.offentligjournal.AvleveringOffentligJournalRoute;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

import static no.nav.dokarkivavlevering.avlevering.testUtils.TestUtils.toLocalDateTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@CamelSpringBootTest
public class AvleveringRouteIT {

	@Autowired
	CamelContext camelContext;

	@Autowired
	private ProducerTemplate template;

	@MockBean
	AsposeService asposeServiceMock;

	@MockBean
	PdlGraphQLConsumer pdlGraphQLConsumer;

	@MockBean
	AvleveringRepository avleveringRepositoryMock;

	@MockBean
	DataSource dataSource_noop;

	@MockBean
	AvleveringSFTPRoute avleveringSFTPRoute_noop;
	private MockEndpoint sftpMock;
	private static final int ANTALL_SAKER = 15;
	private static final Tema TEMA = Tema.MED;

	@BeforeEach
	void before() throws Exception {
		// mock ut endepunktet som timeren sender meldinger til ved oppstart
		mockEndpointAndSkipAt("start_everything", "direct:start_intermediate");
		// mock ut shutdown så appen ikke skrur seg av før testen er ferdig
		mockEndpointAndSkipAt("start_avlevering", AvleveringRoute.SHUTDOWN);

		sftpMock = mockEndpointAndSkipAt("arkivstruktur", AvleveringSFTPRoute.SFTP);
		// wire inn sftp-mock i alle routes vi er innom så vi ikke prøver å koble til en sftp som ikke finnes
		// NB: alle meldingene ender i sftpMock over, men om vi ikke gjør dette blir det bare krøll
		mockEndpointAndSkipAt("send_dokument", AvleveringSFTPRoute.SFTP);

		List<Long> sakIder = LongStream.iterate(1, i -> i + 1).limit(ANTALL_SAKER).boxed().toList();
		List<Long> page1 = sakIder.subList(0, 10);
		List<Long> page2 = sakIder.subList(10, ANTALL_SAKER);

		when(avleveringRepositoryMock.getFagomradeForTema(any())).thenAnswer(args -> getFagomrade(args.getArgument(0)));
		when(avleveringRepositoryMock.findSakIds(any())).thenReturn(sakIder);
		when(avleveringRepositoryMock.findSakerMedDokumenter(eq(page1))).thenReturn(page1.stream().map(id -> newSakWithId(id, TEMA)).toList());
		when(avleveringRepositoryMock.findSakerMedDokumenter(eq(page2))).thenReturn(page2.stream().map(id -> newSakWithId(id, TEMA)).toList());
		when(asposeServiceMock.convertToPDFA(any(), anyLong())).thenReturn("%PDF-1.5\n%âãÏÓ\n1 0 obj".getBytes());
	}

	@Test
	void arkivstruktur_xml_should_be_uploaded_to_sftp_once_with_correct_documents() throws Exception {
		final int forventetAntallPDFDokumenter = ANTALL_SAKER;

		try {
			// mock ut andre routes enn den vi tester
			mockEndpointAndSkipAt("start_avlevering", AvleveringStatiskRoute.AVLEVERING_STATIC);
			mockEndpointAndSkipAt("start_avlevering", AvleveringLoependeJournalRoute.GENERER_LOEPENDEJOURNAL);
			mockEndpointAndSkipAt("start_avlevering", AvleveringEndringsloggRoute.GENERER_ENDRINGSLOGG);
			mockEndpointAndSkipAt("start_avlevering", AvleveringOffentligJournalRoute.GENERER_OFFENTLIGJOURNAL);
			mockEndpointAndSkipAt("start_avlevering", AvleveringArkivuttrekkRoute.GENERER_ARKIVUTTREKK);

			sftpMock.expectedMessageCount(forventetAntallPDFDokumenter + 1);

			template.sendBody("direct:start_avlevering", null);
			sftpMock.assertIsSatisfied();

			assertAntallJournalposterIArkivstruktur(forventetAntallPDFDokumenter);
			sftpMock.reset();
		} finally {
			sftpMock.close();
		}
	}

	private void assertAntallJournalposterIArkivstruktur(int forventetAntallJournalposter) {
		for (int i = 0; i < forventetAntallJournalposter + 1; ++i) {
			Exchange message = sftpMock.assertExchangeReceived(i);
			String filnavn = (String) message.getMessage().getHeader(AvleveringSFTPRoute.HEADER_FILNAVN, "");

			if ("arkivstruktur.xml".equalsIgnoreCase(filnavn)) {
				var body = message.getIn().getBody(File.class);
				try (var fileContent = new FileInputStream(body)) {
					var content = new String(fileContent.readAllBytes());
					var antall = Pattern.compile("<journalsekvensnummer").matcher(content).results().count();
					assertThat(antall).isEqualTo(forventetAntallJournalposter);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private MockEndpoint mockEndpointAndSkipAt(String routeId, String endpointUri) throws Exception {
		AdviceWith.adviceWith(camelContext, routeId, a -> a.mockEndpointsAndSkip(endpointUri));
		return camelContext.getEndpoint("mock:" + endpointUri, MockEndpoint.class);
	}

	private static Sak newSakWithId(long id, Tema tema) {
		return new Sak(id, tema.getTemakode(), "ITest", "Integrasjonstest", LocalDateTime.now(),
				getFagomrade(tema),
				new Bruker("Z123456", "Testesen"), BrukerMedNavnedata.ukjentPerson("01125498765"),
				List.of(generateJournalpost(id)));
	}

	private static Fagomrade getFagomrade(Tema tema) {
		return new Fagomrade(tema.getTemakode(), tema.getTemanavn(), null, LocalDateTime.MIN, "NAV", "1");
	}

	private static Journalpost generateJournalpost(long id) {
		return Journalpost.builder()
				.id(id + 200_000)
				.type("U")
				.status("FS")
				.innhold("Legg til ny institusjon")
				.avsenderMottaker("Bruker Brukersen")
				.datoMottatt(null)
				.datoDokument(toLocalDateTime("2020-11-10 16:05:43.332"))
				.datoJournal(toLocalDateTime("2020-11-10 16:04:43.35"))
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.338"))
				.datoEkspedert(null)
				.datoSendtPrint(null)
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk Jobb")
				.opprettetAvNavn("srvmelosys")
				.endretAv("srvmelosys")
				.endretAvBeriketNavn("Bjarne Betjent")
				.dok(List.of(generateDokumentInfo(id)))
				.ae(List.of(generateArkivendring(id)))
				.build();
	}

	private static Arkivendring generateArkivendring(long id) {
		return Arkivendring.builder()
				.id(id + 100_000)
				.element("uhhh")
				.tidspunkt(toLocalDateTime("2020-11-10 16:05:43.35"))
				.utfoertAv("srvdeluxe")
				.utfoertAvBeriketNavn("deluxe IT system")
				.fraVerdi("1")
				.tilVerdi("2")
				.build();
	}

	private static DokumentInfo generateDokumentInfo(long id) {
		long dokumentInfoId = id + 10_000;
		return DokumentInfo.builder()
				.id(dokumentInfoId)
				.relTilknyttetSom("HOVEDDOKUMENT")
				.relDatoOpprettet(toLocalDateTime("2020-11-10 16:04:43.343"))
				.relOpprettetAv("srvmelosys")
				.relOpprettetAvBeriketNavn("Automatisk jobb")
				.kategoriDecode("Strukturert elektronisk dokument")
				.status("FERDIGSTILT")
				.tittel("Legg til ny institusjon")
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.342"))
				.opprettetAv("srvmelosys")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.fd(List.of(generateFilDetaljer(dokumentInfoId)))
				.ae(List.of(generateArkivendring(dokumentInfoId)))
				.build();
	}

	private static FilDetaljer generateFilDetaljer(long id) {
		return FilDetaljer.builder()
				.id(id + 30_000)
				.filUuid("55c39cdb-f052-4f4e-a9a5-900b455ca915")
				.fil("<html></html>".getBytes())
				.filstorrelseBeriket("<html></html>".length())
				.sha256hashBeriket("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e")
				.datoOpprettet(toLocalDateTime("2020-11-10 16:04:43.343"))
				.opprettetAv("srvRuting")
				.opprettetAvBeriketNavn("Automatisk jobb")
				.build();
	}
}
