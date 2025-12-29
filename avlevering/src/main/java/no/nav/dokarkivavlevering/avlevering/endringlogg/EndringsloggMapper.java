package no.nav.dokarkivavlevering.avlevering.endringlogg;

import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.endringslogg.Endring;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.domain.Arkivendring.INGEN_VERDI;
import static no.nav.dokarkivavlevering.avlevering.endringlogg.ReferanseMetadataMapper.mapArkivElementToReferanseMetadata;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@Profile("genererAvlevering")
public class EndringsloggMapper {

	private static final String JOURNALPOST_JOURNALPOSTSTATUS = "Journalpost.journalpostStatus";
	private static final String SELVE_ORDET_NULL = "null";
	private static final Set<String> SKAL_IKKE_AVLEVERES_ENDRINGSLOGG = Set.of("N/A");

	public Optional<Endring> map(Arkivendring arkivendring, UUID uuid) {
		boolean endringIsJournalstatus = JOURNALPOST_JOURNALPOSTSTATUS.equals(arkivendring.getElement());
		String referanseMetadata = mapArkivElementToReferanseMetadata(arkivendring.getElement());
		String tidligereVerdi = mapVerdi(arkivendring.getFraVerdi(), endringIsJournalstatus);
		String nyVerdi = mapVerdi(arkivendring.getTilVerdi(), endringIsJournalstatus);

		if (INGEN_VERDI.equals(tidligereVerdi) && INGEN_VERDI.equals(nyVerdi) || SKAL_IKKE_AVLEVERES_ENDRINGSLOGG.contains(referanseMetadata)) {
			return Optional.empty();
		} else if (uuid == null && isBlank(arkivendring.getElement())) {
			log.warn("arkivendring er null og kan ikke mappe endringslogg");
			return Optional.empty();
		} else {
			Endring endring = new Endring();
			endring.setReferanseArkivenhet(uuid.toString());
			endring.setReferanseMetadata(referanseMetadata);
			endring.setEndretDato(arkivendring.getTidspunkt());
			endring.setEndretAv(arkivendring.getUtfoertAvBeriketNavn());
			endring.setTidligereVerdi(tidligereVerdi);
			endring.setNyVerdi(nyVerdi);
			return Optional.of(endring);
		}
	}

	private String mapVerdi(String verdi, boolean isJournalstatusEndring) {
		if (isBlank(verdi) || SELVE_ORDET_NULL.equalsIgnoreCase(verdi.trim())) {
			return INGEN_VERDI;
		} else if (isJournalstatusEndring) {
			try {
				return JournalpostStatus.valueOf(verdi).statusDecode;
			} catch (IllegalArgumentException e) {
				return "Ukjent status: " + verdi;
			}
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
