package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getFagomradeBeskrivelse;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapSystemID;

@Component
public class ArkivdelMapper {

	private final AvleveringProperties.ArkivConfig arkivConfig;

	public ArkivdelMapper(AvleveringProperties avleveringProperties) {
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	public Arkivdel map(Fagomrade fagomrade, List<Saksmappe> saksmapper) {
		Arkivdel arkivdel = new Arkivdel();

		SystemID systemID = mapSystemID(UUID.randomUUID().toString());
		arkivdel.setSystemID(systemID);
		saksmapper.forEach(mappe -> mappe.getReferanseArkivdels().add(systemID.getValue()));
		arkivdel.getMappes().addAll(saksmapper);

		arkivdel.setTittel(fagomrade.getDekode());
		arkivdel.setBeskrivelse(getFagomradeBeskrivelse(fagomrade.getFagomrade()));
		arkivdel.setArkivdelstatus(getArkivdelstatus(fagomrade));
		arkivdel.setOpprettetDato(fagomrade.getDatoOpprettet());
		arkivdel.setOpprettetAv("Arbeids- og velferdsetaten");
		arkivdel.setArkivperiodeStartDato(fagomrade.getDatoOpprettet().toLocalDate());
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

	private static Skjerming mapSkjerming() {
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
