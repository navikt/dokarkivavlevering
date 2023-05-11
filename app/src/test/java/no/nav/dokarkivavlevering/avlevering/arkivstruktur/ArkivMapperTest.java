package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivskaper;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_TIME_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivMapperTest {

	public static final Tema SAK_TEMA = Tema.IAR;
	private final ArkivMapper arkivMapper = new ArkivMapper(new AvleveringProperties());

	@Test
	void shouldMap() {
		final Arkiv arkiv = arkivMapper.map(Collections.emptyList());
		assertThat(arkiv.getSystemID().getValue()).isNotEmpty();
		assertThat(arkiv.getTittel()).isEqualTo("NAV Fagarkiv");
		assertThat(arkiv.getBeskrivelse()).isEqualTo("Fagarkivet dokumenterer behandlingen av enkeltsaker knyttet til en bruker – person eller organisasjon – som etter lov om arbeids- og velferdsforvaltningen har satt fram søknad om ytelser, tiltak og oppfølging for Arbeids- og velferdsetaten");
		assertThat(arkiv.getDokumentmedium()).isEqualTo("Elektronisk arkiv");
		assertThat(toDateTimeString(arkiv.getOpprettetDato())).isEqualTo("2008-12-01T12:00:00");
		assertThat(arkiv.getOpprettetAv()).isEqualTo("Arbeids- og velferdsetaten");
		assertArkivskaper(arkiv);
	}

	private void assertArkivskaper(Arkiv arkiv) {
		assertThat(arkiv.getArkivskapers()).hasSize(1);
		final Arkivskaper arkivskaper = arkiv.getArkivskapers().get(0);
		assertThat(arkivskaper.getArkivskaperID()).isEqualTo("889 640 782");
		assertThat(arkivskaper.getArkivskaperNavn()).isEqualTo("Arbeids- og velferdsetaten");
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

	private String toDateString(LocalDate localDate) {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
	}

	private String toDateTimeString(LocalDateTime localDateTime) {
		return DATE_TIME_FORMAT.format(localDateTime);
	}
}