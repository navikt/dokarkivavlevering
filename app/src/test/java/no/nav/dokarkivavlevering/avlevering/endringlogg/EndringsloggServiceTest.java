package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EndringsloggServiceTest {

	@Mock
	private EndringsloggMapper endringsloggMapper;

	@Captor
	private ArgumentCaptor<Arkivendring> arkivendringArgumentCaptor;

	@InjectMocks
	private EndringsloggService endringsloggService;

	private final LocalDate journalfoeringsDato = LocalDate.now().minusDays(3);

	@Test
	void shouldMapEndringerAfterJournalDato() {
		List<Sak> saker = opprettSaker();
		List<Endring> endringer = endringsloggService.avlevering(saker);

		verify(endringsloggMapper, times(2)).map(arkivendringArgumentCaptor.capture(), any());

		List<Arkivendring> utvalgteArkivendringerListe = arkivendringArgumentCaptor.getAllValues();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(endringer.size()).isEqualTo(2);
		softly.assertThat(utvalgteArkivendringerListe.get(0).getTidspunkt()).isAfter(Date.valueOf(journalfoeringsDato));
		softly.assertThat(utvalgteArkivendringerListe.get(0).getElement()).isEqualTo("Test1");
		softly.assertThat(utvalgteArkivendringerListe.get(1).getTidspunkt()).isAfter(Date.valueOf(journalfoeringsDato));
		softly.assertThat(utvalgteArkivendringerListe.get(1).getElement()).isEqualTo("Test4");
		softly.assertAll();
	}

	private List<Sak> opprettSaker() {
		return asList(Sak.builder()
				.jp(opprettJournalpost())
				.build());
	}

	private List<Journalpost> opprettJournalpost() {
		return asList(Journalpost.builder()
				.datoJournal(Date.valueOf(journalfoeringsDato))
				.ae(opprettArkivendring())
				.dok(opprettDokumentInfo())
				.build());
	}

	private List<Arkivendring> opprettArkivendring() {
		return asList(Arkivendring.builder()
						.tidspunkt(Date.valueOf(LocalDate.now()))
						.element("Test1")
						.build(),
				Arkivendring.builder()
						.tidspunkt(Date.valueOf(LocalDate.now().minusDays(10)))
						.element("Test2")
						.build());
	}

	private List<DokumentInfo> opprettDokumentInfo() {
		return asList(DokumentInfo.builder()
				.ae(asList(
						Arkivendring.builder()
								.tidspunkt(Date.valueOf(LocalDate.now().minusDays(7)))
								.element("Test3")
								.build(),
						Arkivendring.builder()
								.tidspunkt(Date.valueOf(LocalDate.now().minusDays(1)))
								.element("Test4")
								.build())
				).build()
		);
	}
}