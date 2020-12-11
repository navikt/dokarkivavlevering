package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class AvleveringSakBerikerMapper {
	private static final String AUTOMATISK_JOBB = "Automatisk Jobb";

	Sak berik(final Sak sak, final Map<String, String> navAnsatteNavn, Map<String, Bruker> pdlHentIdenterBolks, Map<String, Bruker> eregOrganisasjonBolk) {
		return sak.toBuilder()
				.bruker(mapBruker(sak.getBruker(), pdlHentIdenterBolks, eregOrganisasjonBolk))
				.opprettetAvBeriketNavn(utledNavn(sak.getOpprettetAv(), navAnsatteNavn))
				.jp(sak.getJp().stream().map(journalpost -> {
					return journalpost.toBuilder()
							.opprettetAvBeriketNavn(utledNavn(journalpost.getOpprettetAv(), navAnsatteNavn))
							.endretAvBeriketNavn(utledNavn(journalpost.getEndretAv(), navAnsatteNavn))
							.dok(journalpost.getDok().stream()
									.map(dokumentInfo -> {
										return dokumentInfo.toBuilder()
												.opprettetAvBeriketNavn(utledNavn(dokumentInfo.getOpprettetAv(), navAnsatteNavn))
												.relOpprettetAvBeriketNavn(utledNavn(dokumentInfo.getRelOpprettetAv(), navAnsatteNavn))
												.fd(dokumentInfo.getFd().stream()
														.map(filDetaljer -> {
															return filDetaljer.toBuilder()
																	.opprettetAvBeriketNavn(utledNavn(filDetaljer.getOpprettetAv(), navAnsatteNavn))
																	.filstorrelseBeriket(filDetaljer.getFil().length)
																	.sha256hashBeriket(DigestUtils.sha256Hex(filDetaljer.getFil()))
																	.build();
														})
														.collect(Collectors.toList()))
												.ae(dokumentInfo.getAe().stream()
														.map(arkivendring -> {
															return arkivendring.toBuilder()
																	.utfoertAvBeriketNavn(utledNavn(arkivendring.getUtfoertAv(), navAnsatteNavn))
																	.build();
														})
														.collect(Collectors.toList()))
												.build();
									})
									.collect(Collectors.toList()))
							.ae(journalpost.getAe().stream()
									.map(arkivendring -> {
										return arkivendring.toBuilder()
												.utfoertAvBeriketNavn(utledNavn(arkivendring.getUtfoertAv(), navAnsatteNavn))
												.build();
									})
									.collect(Collectors.toList()))
							.build();
				}).collect(Collectors.toList()))
				.build();
	}

	private Bruker mapBruker(Bruker bruker, Map<String, Bruker> pdlHentIdenterBolks, Map<String, Bruker> eregOrganisasjonBolk) {
		final String brukerId = bruker.getId();
		if (bruker.isPerson()) {
			return pdlHentIdenterBolks.getOrDefault(brukerId, Bruker.ukjentPerson(brukerId));
		} else {
			return eregOrganisasjonBolk.getOrDefault(brukerId, Bruker.ukjentOrganisasjon(brukerId));
		}
	}

	private String utledNavn(final String adeoIdent, final Map<String, String> navAnsatteNavn) {
		return navAnsatteNavn.getOrDefault(adeoIdent, AUTOMATISK_JOBB);
	}
}
