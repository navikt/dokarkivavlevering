package no.nav.dokarkivavlevering.avlevering.endringlogg;

import lombok.Getter;
import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.dateToXMLGregorianCalendar;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class EndringsloggMapper {

	private static final String JOURNALPOST_JOURNALPOSTSTATUS = "Journalpost.journalpostStatus";

	public Endring map(Arkivendring arkivendring, UUID uuid) {
		Endring endring = new Endring();
		endring.setReferanseArkivenhet(uuid.toString());
		endring.setReferanseMetadata(arkivendring.getElement());
		endring.setEndretDato(dateToXMLGregorianCalendar(arkivendring.getTidspunkt()));
		endring.setEndretAv(arkivendring.getUtfoertAvBeriketNavn());


		endring.setTidligereVerdi(mapTidligereVerdi(arkivendring));
		endring.setNyVerdi(mapNyVerdi(arkivendring));
		return endring;
	}

	private String mapNyVerdi(Arkivendring arkivendring){
		return JOURNALPOST_JOURNALPOSTSTATUS.equals(arkivendring.getElement()) ?
				journalpostStatusDecode(arkivendring.getTilVerdi()) : arkivendring.getTilVerdi();
	}

	private String mapTidligereVerdi(Arkivendring arkivendring) {
		if(isBlank(arkivendring.getFraVerdi())) {
			return Arkivendring.INGEN_VERDI;
		} else if(JOURNALPOST_JOURNALPOSTSTATUS.equals(arkivendring.getElement())){
			return journalpostStatusDecode(arkivendring.getFraVerdi());
		}
		return arkivendring.getFraVerdi();
	}

	private String journalpostStatusDecode(String journalpostStatus){
		return isEmpty(journalpostStatus) ?
				JournalpostStatus.UKJENT_STATUS : JournalpostStatus.valueOf(journalpostStatus).getStatusDecode();
	}

	@Getter
	private enum JournalpostStatus {
		J("J", "JOURNALFØRT"),
		M("M", "MOTTATT"),
		U("U", "UTGAAR"),
		D("D", "UNDER_ARBEID"),
		R("R", "RESERVERT"),
		FS("FS", "FERDIGSTILT"),
		FL("FL", "FERDIGSTILT"),
		E("E", "EKSPEDERT"),
		A("A", "AVBRUTT"),
		MO("MO", "MOTTATT"),
		UB("UB", "UKJENT_BRUKER"),
		OD("OD", "OPPLASTING_DOKUMENT");

		public static String UKJENT_STATUS = "UKJENT";
		private final String statusCode;
		private final String statusDecode;

		JournalpostStatus(String statusCode, String statusDecode){
			this.statusCode = statusCode;
			this.statusDecode = statusDecode;
		}

	}
}
