package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivskaper;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDate;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_TIME_FORMAT;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getFagomradeBeskrivelse;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapXmlGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.truncateToDate;


@Component
public class ArkivMapper {

	private final AvleveringProperties.ArkivConfig arkivConfig;

	public ArkivMapper(AvleveringProperties avleveringProperties) {
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	@Handler
	public Arkiv map(Sak sak) {
		Arkiv arkiv = new Arkiv();
		arkiv.setSystemID(AvleveringUtils.mapSystemID(arkivConfig.getSystemID()));
		arkiv.setTittel("NAV Fagarkiv");
		arkiv.setBeskrivelse("Fagarkivet dokumenterer behandlingen av enkeltsaker knyttet til en bruker – person eller organisasjon – som etter lov om arbeids- og velferdsforvaltningen har satt fram søknad om ytelser, tiltak og oppfølging for Arbeids- og velferdsetaten");
		arkiv.setDokumentmedium("Elektronisk arkiv");
		arkiv.setOpprettetDato(mapXmlGregorianCalendar(DATE_TIME_FORMAT, "2008-12-01T12:00:00"));
		arkiv.setOpprettetAv("Arbeids- og velferdsetaten");
		arkiv.getArkivskapers().add(mapArkivskaper());
		arkiv.getArkivdels().add(mapArkivdel(sak));
		return arkiv;
	}

	private Arkivskaper mapArkivskaper() {
		Arkivskaper arkivskaper = new Arkivskaper();
		arkivskaper.setArkivskaperID("889 640 782");
		arkivskaper.setArkivskaperNavn("Arbeids- og velferdsetaten");
		return arkivskaper;
	}

	private Arkivdel mapArkivdel(Sak sak) {
		Arkivdel arkivdel = new Arkivdel();
		arkivdel.setSystemID(AvleveringUtils.mapSystemID(arkivConfig.getArkivdelConfig().getSystemID()));
		Fagomrade fagomrade = sak.getFagomrade();
		arkivdel.setTittel(fagomrade.getDekode());
		arkivdel.setBeskrivelse(getFagomradeBeskrivelse(fagomrade.getFagomrade()));
		arkivdel.setArkivdelstatus(getArkivdelstatus(fagomrade));
		arkivdel.setOpprettetDato(mapXmlGregorianCalendar(fagomrade.getDatoOpprettet()));
		arkivdel.setOpprettetAv("Arbeids- og velferdsetaten");
		arkivdel.setArkivperiodeStartDato(truncateToDate(mapXmlGregorianCalendar(fagomrade.getDatoOpprettet())));
		arkivdel.setSkjerming(mapSkjerming());
		arkivdel.getKlassifikasjonssystems().add(mapKlassifikasjonssystem(arkivdel));
		return arkivdel;
	}

	private static String getArkivdelstatus(Fagomrade fagomrade) {
		if (!fagomrade.erGyldig() && fagomrade.getDatoTom() != null && LocalDate.now().isAfter(fagomrade.getDatoTom())) {
			return "Avsluttet periode";
		}
		return "Aktiv periode";
	}

	private Skjerming mapSkjerming() {
		Skjerming skjerming = new Skjerming();
		skjerming.setTilgangsrestriksjon("Unntatt offentlighet");
		skjerming.setSkjermingshjemmel("Offl § 13, jf fvl § 13 og lov om arbeids- og velferdsforvaltningen § 7");
		skjerming.getSkjermingMetadatas().add("Skjerming navn part i sak");
		skjerming.getSkjermingMetadatas().add("Skjerming navn avsender");
		skjerming.getSkjermingMetadatas().add("Skjerming navn mottaker");
		skjerming.setSkjermingDokument("Skjerming av hele dokumentet");
		skjerming.setSkjermingsvarighet(new BigInteger("60"));
		return skjerming;
	}

	private Klassifikasjonssystem mapKlassifikasjonssystem(Arkivdel arkivdel) {
		Klassifikasjonssystem klassifikasjonssystem = new Klassifikasjonssystem();
		klassifikasjonssystem.setSystemID(AvleveringUtils.generateSystemId());
		klassifikasjonssystem.setKlassifikasjonstype("Fagområder i NAV");
		klassifikasjonssystem.setTittel("Fagområder i NAV");
		klassifikasjonssystem.setOpprettetDato(arkivdel.getOpprettetDato());
		klassifikasjonssystem.setOpprettetAv("Arbeids- og velferdsetaten");
		return klassifikasjonssystem;
	}

}
