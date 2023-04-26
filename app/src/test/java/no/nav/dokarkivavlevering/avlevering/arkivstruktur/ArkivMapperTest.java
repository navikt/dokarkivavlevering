package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivskaper;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.NavnMedGyldighet;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_FORMAT;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_TIME_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivMapperTest {

	public static final Tema SAK_TEMA = Tema.IAR;
	private final ArkivMapper arkivMapper = new ArkivMapper(new AvleveringProperties());

	private final Sak TEST_SAK = new Sak(1L, SAK_TEMA.name(), "NAV", "NAV", new Date(),
			new Fagomrade(SAK_TEMA.name(), SAK_TEMA.getTemanavn(), LocalDate.of(2008,12,1),
					LocalDate.now().plusYears(2), LocalDateTime.of(2010, 2, 18, 12, 0, 0), "NAV",
					LocalDateTime.now().minus(3, ChronoUnit.YEARS), "NAV", "1"),
			new Bruker("2", "Ukjent"),
			new BrukerMedNavnedata(	"2", List.of(new NavnMedGyldighet(ZonedDateTime.now().minusYears(10), ZonedDateTime.now().minusYears(2), "Ugyldigsen"), new NavnMedGyldighet(ZonedDateTime.now().minusYears(2), null, "Brukersen"))),  emptyList());

	@Test
	void shouldMap() {
		final Arkiv arkiv = arkivMapper.map(TEST_SAK);
		assertThat(arkiv.getSystemID().getValue()).isNotEmpty();
		assertThat(arkiv.getTittel()).isEqualTo("NAV Fagarkiv");
		assertThat(arkiv.getBeskrivelse()).isEqualTo("Fagarkivet dokumenterer behandlingen av enkeltsaker knyttet til en bruker – person eller organisasjon – som etter lov om arbeids- og velferdsforvaltningen har satt fram søknad om ytelser, tiltak og oppfølging for Arbeids- og velferdsetaten");
		assertThat(arkiv.getDokumentmedium()).isEqualTo("Elektronisk arkiv");
		assertThat(toDateTimeString(arkiv.getOpprettetDato())).isEqualTo("2008-12-01T12:00:00");
		assertThat(arkiv.getOpprettetAv()).isEqualTo("Arbeids- og velferdsetaten");
		assertArkivskaper(arkiv);
		assertArkivdel(arkiv);
	}

	private void assertArkivskaper(Arkiv arkiv) {
		assertThat(arkiv.getArkivskapers()).hasSize(1);
		final Arkivskaper arkivskaper = arkiv.getArkivskapers().get(0);
		assertThat(arkivskaper.getArkivskaperID()).isEqualTo("889 640 782");
		assertThat(arkivskaper.getArkivskaperNavn()).isEqualTo("Arbeids- og velferdsetaten");
		assertThat(arkiv.getArkivdels()).hasSize(1);
	}

	private void assertArkivdel(Arkiv arkiv) {
		assertThat(arkiv.getArkivdels()).hasSize(1);
		final Arkivdel arkivdel = arkiv.getArkivdels().get(0);
		assertThat(arkivdel.getSystemID().getValue()).isNotEmpty();
		assertThat(arkivdel.getTittel()).isEqualTo("Inkluderende arbeidsliv");
		assertThat(arkivdel.getBeskrivelse()).isEqualTo("Intensjonsavtalen om et mer inkluderende arbeidsliv: Samarbeidsavtaler, mål- og handlingsplaner. Noe tilskudd");
		assertThat(arkivdel.getArkivdelstatus()).isEqualTo("Aktiv periode");
		assertThat(toDateTimeString(arkivdel.getOpprettetDato())).isEqualTo("2010-02-18T12:00:00");
		assertThat(arkivdel.getOpprettetAv()).isEqualTo("Arbeids- og velferdsetaten");
		assertThat(toDateString(arkivdel.getArkivperiodeStartDato())).isEqualTo("2010-02-18");
		assertArkivdelSkjerming(arkivdel);
		assertArkivdelKlassifikasjonssystem(arkivdel);
	}

	private void assertArkivdelKlassifikasjonssystem(Arkivdel arkivdel) {
		assertThat(arkivdel.getKlassifikasjonssystems()).hasSize(1);
		final Klassifikasjonssystem klassifikasjonssystem = arkivdel.getKlassifikasjonssystems().get(0);
		assertThat(klassifikasjonssystem.getSystemID()).isNotNull();
		assertThat(klassifikasjonssystem.getKlassifikasjonstype()).isEqualTo("Fagområder i NAV");
		assertThat(klassifikasjonssystem.getTittel()).isEqualTo("Fagområder i NAV");
		assertThat(klassifikasjonssystem.getBeskrivelse()).isNull();
		assertThat(toDateTimeString(klassifikasjonssystem.getOpprettetDato())).isEqualTo("2010-02-18T12:00:00");
		assertThat(klassifikasjonssystem.getOpprettetAv()).isEqualTo("Arbeids- og velferdsetaten");
	}

	private void assertArkivdelSkjerming(Arkivdel arkivdel) {
		final Skjerming skjerming = arkivdel.getSkjerming();
		assertThat(skjerming.getTilgangsrestriksjon()).isEqualTo("Unntatt offentlighet");
		assertThat(skjerming.getSkjermingshjemmel()).isEqualTo("Offl § 13, jf fvl § 13 og lov om arbeids- og velferdsforvaltningen § 7");
		assertThat(skjerming.getSkjermingMetadatas()).hasSize(3).contains("Skjerming navn part i sak", "Skjerming navn avsender", "Skjerming navn mottaker");
		assertThat(skjerming.getSkjermingDokument()).isEqualTo("Skjerming av hele dokumentet");
		assertThat(skjerming.getSkjermingsvarighet()).isEqualTo(new BigInteger("60"));
	}

	private String toDateString(final XMLGregorianCalendar xmlGregorianCalendar) {
		return DATE_FORMAT.format(xmlGregorianCalendar.toGregorianCalendar().getTime());
	}

	private String toDateTimeString(final XMLGregorianCalendar xmlGregorianCalendar) {
		return DATE_TIME_FORMAT.format(xmlGregorianCalendar.toGregorianCalendar().getTime());
	}
}