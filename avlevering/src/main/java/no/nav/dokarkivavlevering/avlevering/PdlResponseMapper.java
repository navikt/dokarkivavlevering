package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.NavnMedGyldighet;
import no.nav.dokarkivavlevering.avlevering.domain.SimpleNavn;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlHentPersonBolkResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static no.nav.dokarkivavlevering.avlevering.domain.Bruker.UKJENT_PERSON;

public class PdlResponseMapper {

	public static Map<String, BrukerMedNavnedata> mapPdlHentIdenterPersonBolk(List<PdlHentPersonBolkResponse.PdlHentPersonBolk> hentPersonBolk) {
		Map<String, BrukerMedNavnedata> brukerMedNavnedataMap = new HashMap<>();
		for (PdlHentPersonBolkResponse.PdlHentPersonBolk personbolk : hentPersonBolk) {
			brukerMedNavnedataMap.put(personbolk.getIdent(), toBrukerMedNavnedata(personbolk));
		}
		return brukerMedNavnedataMap;
	}

	private static BrukerMedNavnedata toBrukerMedNavnedata(PdlHentPersonBolkResponse.PdlHentPersonBolk personbolk) {
		return new BrukerMedNavnedata(folkeregisterIdentifikator(personbolk.getPerson()), getNavnMedGyldighet(personbolk));
	}

	private static List<NavnMedGyldighet> getNavnMedGyldighet(PdlHentPersonBolkResponse.PdlHentPersonBolk personbolk) {
		if (personbolk.getPerson() == null) {
			return List.of(new NavnMedGyldighet(null, null, UKJENT_PERSON));
		}
		return personbolk.getPerson().getNavn().stream().map(PdlResponseMapper::toNavnMedGyldighet).toList();
	}

	private static NavnMedGyldighet toNavnMedGyldighet(PdlHentPersonBolkResponse.PdlNavn pdlNavn) {
		PdlHentPersonBolkResponse.PdlFolkeregistermetadata folkeregistermetadata = pdlNavn.getPdlFolkeregistermetadata();
		if (folkeregistermetadata == null) {
			return new SimpleNavn(fulltnavn(pdlNavn));
		}
		return new NavnMedGyldighet(parseZonedDateTime(folkeregistermetadata.getGyldighetstidspunkt()),
				parseZonedDateTime(folkeregistermetadata.getOpphoerstidspunkt()), fulltnavn(pdlNavn));
	}

	private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");

	private static ZonedDateTime parseZonedDateTime(String tidspunkt) {
		if (tidspunkt == null) {
			return null;
		}
		return LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(tidspunkt)).atZone(OSLO);
	}

	private static String folkeregisterIdentifikator(PdlHentPersonBolkResponse.PdlPerson person){
		if (isNull(person)) {
			return UKJENT_PERSON;
		}
		return person.getFolkeregisteridentifikator().stream()
				.map(PdlHentPersonBolkResponse.PdlFolkeregisteridentifikator::getIdentifikasjonsnummer)
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(UKJENT_PERSON);

	}

	private static String fulltnavn(PdlHentPersonBolkResponse.PdlNavn pdlNavn) {
		return Stream.of(pdlNavn.getFornavn(), pdlNavn.getMellomnavn(), pdlNavn.getEtternavn())
				.filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}
}
