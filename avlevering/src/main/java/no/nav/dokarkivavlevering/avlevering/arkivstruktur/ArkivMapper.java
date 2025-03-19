package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivskaper;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_TIME_FORMAT;


@Component
public class ArkivMapper {

	public static final LocalDateTime ARKIV_OPPRETTET_TIDSPUNKT = LocalDateTime.from(DATE_TIME_FORMAT.parse("2008-12-01T12:00:00"));

	private final DokarkivavleveringProperties.ArkivConfig arkivConfig;

	public ArkivMapper(DokarkivavleveringProperties avleveringProperties) {
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	@Handler
	public Arkiv map(List<Arkivdel> arkivdelList) {
		Arkiv arkiv = new Arkiv();
		arkiv.setSystemID(AvleveringUtils.mapSystemID(arkivConfig.getSystemID()));
		arkiv.setTittel("NAV Fagarkiv");
		arkiv.setBeskrivelse("Fagarkivet dokumenterer behandlingen av enkeltsaker knyttet til en bruker – person eller organisasjon – som etter lov om arbeids- og velferdsforvaltningen har satt fram søknad om ytelser, tiltak og oppfølging for Arbeids- og velferdsetaten");
		arkiv.setDokumentmedium("Elektronisk arkiv");
		arkiv.setOpprettetDato(ARKIV_OPPRETTET_TIDSPUNKT);
		arkiv.setOpprettetAv("Arbeids- og velferdsetaten");
		arkiv.getArkivskapers().add(mapArkivskaper());
		arkiv.getArkivdels().addAll(arkivdelList);
		return arkiv;
	}

	private static Arkivskaper mapArkivskaper() {
		Arkivskaper arkivskaper = new Arkivskaper();
		arkivskaper.setArkivskaperID("889 640 782");
		arkivskaper.setArkivskaperNavn("Arbeids- og velferdsetaten");
		return arkivskaper;
	}
}
