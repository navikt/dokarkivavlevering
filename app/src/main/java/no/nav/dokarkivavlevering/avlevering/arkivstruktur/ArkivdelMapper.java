package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.generateSystemID;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.getFagomradeBeskrivelse;

@Component
public class ArkivdelMapper {

	public Arkivdel map(Fagomrade fagomrade, Klasse klasse) {
		Arkivdel arkivdel = new Arkivdel();

		SystemID arkivdelSystemID = generateSystemID();
		arkivdel.setSystemID(arkivdelSystemID);
		klasse.getMappes().forEach(mappe ->
				mappe.getReferanseArkivdels().add(arkivdelSystemID.getValue()));

		arkivdel.setTittel(fagomrade.getDekode());
		arkivdel.setBeskrivelse(getFagomradeBeskrivelse(fagomrade.getFagomrade()));
		arkivdel.setArkivdelstatus(getArkivdelstatus(fagomrade));
		arkivdel.setOpprettetDato(fagomrade.getDatoOpprettet());
		arkivdel.setOpprettetAv("Arbeids- og velferdsetaten");
		arkivdel.setArkivperiodeStartDato(fagomrade.getDatoOpprettet().toLocalDate());
		arkivdel.setSkjerming(mapSkjerming());
		arkivdel.getKlassifikasjonssystems().add(mapKlassifikasjonssystem(fagomrade, klasse));
		return arkivdel;
	}

	private static String getArkivdelstatus(Fagomrade fagomrade) {
		if (fagomrade.erGyldigAkkuratNaa()) {
			return "Aktiv periode";
		}
		return "Avsluttet periode";
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

	private Klassifikasjonssystem mapKlassifikasjonssystem(Fagomrade fagomrade, Klasse klasse) {
		Klassifikasjonssystem klassifikasjonssystem = new Klassifikasjonssystem();
		klassifikasjonssystem.setSystemID(AvleveringUtils.generateSystemID());
		klassifikasjonssystem.setKlassifikasjonstype("Fagområder i NAV");
		klassifikasjonssystem.setTittel("Fagområder i NAV");
		klassifikasjonssystem.setOpprettetDato(fagomrade.getDatoOpprettet());
		klassifikasjonssystem.setOpprettetAv("Arbeids- og velferdsetaten");
		klassifikasjonssystem.getKlasses().add(klasse);
		return klassifikasjonssystem;
	}

}
