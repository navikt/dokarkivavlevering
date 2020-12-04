package no.nav.dokarkivavlevering.avlevering.endringlogg;

import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndringsloggServiceTest {

	@Mock
	private EndringsloggMapper endringsloggMapper;

	@InjectMocks
	private EndringsloggService endringsloggService;

	@Test
	void shouldMapEndringerAfterJournalDato() {
		when(endringsloggMapper.map(any(), any())).thenReturn(new Endring());

		List<Sak> saker = opprettSaker();
		List<Endring> endringer = endringsloggService.avlevering(saker);
		assertThat(endringer.size()).isEqualTo(2);
	}

	private List<Sak> opprettSaker() {
		return asList(Sak.builder()
				.jp(opprettJournalpost())
				.build());
	}

	private List<Journalpost> opprettJournalpost() {
		return asList(Journalpost.builder()
				.datoJournal(Date.valueOf(LocalDate.now().minusDays(3)))
				.ae(opprettArkivendring())
				.dok(opprettDokumentInfo())
				.build());
	}

	private List<Arkivendring> opprettArkivendring() {
		return asList(Arkivendring.builder()
						.tidspunkt(Date.valueOf(LocalDate.now()))
						.element("Saksrelasjon.sakId")
						.build(),
				Arkivendring.builder()
						.tidspunkt(Date.valueOf(LocalDate.now().minusDays(10)))
						.element("DokumentInfo.info")
						.build());
	}

	private List<DokumentInfo> opprettDokumentInfo() {
		return asList(DokumentInfo.builder()
				.ae(Collections.singletonList(
						Arkivendring.builder()
								.tidspunkt(Date.valueOf(LocalDate.now().minusDays(1)))
								.build())
				).build()

		);
	}
}