package no.nav.dokarkivavlevering.avlevering.endringlogg;

import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.domain.Arkivendring.INGEN_VERDI;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@Profile("genererAvlevering")
public class EndringsloggMapper {

	private static final String JOURNALPOST_JOURNALPOSTSTATUS = "Journalpost.journalpostStatus";

	public Optional<Endring> map(Arkivendring arkivendring, UUID uuid) {
		boolean endringIsJournalstatus = JOURNALPOST_JOURNALPOSTSTATUS.equals(arkivendring.getElement());
		String tidligereVerdi = mapVerdi(arkivendring.getFraVerdi(), endringIsJournalstatus);
		String nyVerdi = mapVerdi(arkivendring.getTilVerdi(), endringIsJournalstatus);

		if (INGEN_VERDI.equals(tidligereVerdi) && INGEN_VERDI.equals(nyVerdi)) {
			return Optional.empty();
		} else if (uuid == null && isBlank(arkivendring.getElement())) {
			log.warn("arkivendring er null og kan ikke mappe endringslogg");
			return Optional.empty();
		} else {
			Endring endring = new Endring();
			endring.setReferanseArkivenhet(uuid.toString());
			endring.setReferanseMetadata(arkivendring.getElement());
			endring.setEndretDato(arkivendring.getTidspunkt());
			endring.setEndretAv(arkivendring.getUtfoertAvBeriketNavn());
			endring.setTidligereVerdi(tidligereVerdi);
			endring.setNyVerdi(nyVerdi);
			return Optional.of(endring);
		}
	}

	private String mapVerdi(String verdi, boolean isJournalstatusEndring) {
		if (isBlank(verdi)) {
			return INGEN_VERDI;
		} else if (isJournalstatusEndring) {
			return JournalpostStatus.valueOf(verdi).statusDecode;
		} else {
			return verdi;
		}
	}

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

		public final String statusCode;
		public final String statusDecode;

		JournalpostStatus(String statusCode, String statusDecode) {
			this.statusCode = statusCode;
			this.statusDecode = statusDecode;
		}

	}
}
