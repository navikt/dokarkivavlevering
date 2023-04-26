package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.aspose.AsposeService;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Component
public class AvleveringSakBerikerMapper {
	private static final String AUTOMATISK_JOBB = "Automatisk Jobb";
	private final AsposeService asposeService;

	public AvleveringSakBerikerMapper(AsposeService asposeService) {
		this.asposeService = asposeService;
	}

	Sak berikMedDokumenter(final Sak sak, final Map<String, String> navAnsatteNavn, Map<String, BrukerMedNavnedata> pdlHentIdenterBolks, Map<String, BrukerMedNavnedata> eregOrganisasjonBolk) {
		return berikMedSpesifisertOperasjonForDokument(sak, navAnsatteNavn, pdlHentIdenterBolks, eregOrganisasjonBolk, this::berikMedFil);
	}

	Sak berikUtenDokument(final Sak sak, final Map<String, String> navAnsatteNavn, Map<String, BrukerMedNavnedata> pdlHentIdenterBolks, Map<String, BrukerMedNavnedata> eregOrganisasjonBolk) {
		return berikMedSpesifisertOperasjonForDokument(sak, navAnsatteNavn, pdlHentIdenterBolks, eregOrganisasjonBolk, (__, ___) -> null);
	}

	private Sak berikMedSpesifisertOperasjonForDokument(Sak sak, Map<String, String> navAnsatteNavn, Map<String, BrukerMedNavnedata> pdlHentIdenterBolks, Map<String, BrukerMedNavnedata> eregOrganisasjonBolk,
														BiFunction<Map<String,String>,DokumentInfo,List<FilDetaljer>> berikDokumentOperasjon) {
		return sak.toBuilder()
				.brukerMedNavnedata(mapBruker(sak.getBruker(), pdlHentIdenterBolks, eregOrganisasjonBolk))
				.opprettetAvBeriketNavn(utledNavn(sak.getOpprettetAv(), navAnsatteNavn))
				.jp(sak.getJp().stream().map(journalpost -> journalpost.toBuilder()
						.opprettetAvBeriketNavn(utledNavn(journalpost.getOpprettetAv(), navAnsatteNavn))
						.endretAvBeriketNavn(utledNavn(journalpost.getEndretAv(), navAnsatteNavn))
						.dok(journalpost.getDok().stream()
								.map(dokumentInfo -> dokumentInfo.toBuilder()
										.opprettetAvBeriketNavn(utledNavn(dokumentInfo.getOpprettetAv(), navAnsatteNavn))
										.relOpprettetAvBeriketNavn(utledNavn(dokumentInfo.getRelOpprettetAv(), navAnsatteNavn))
										.fd(berikDokumentOperasjon.apply(navAnsatteNavn, dokumentInfo))
										.ae(berikArkivendringer(dokumentInfo.getAe(), navAnsatteNavn))
										.build())
								.toList())
						.ae(berikArkivendringer(journalpost.getAe(), navAnsatteNavn))
						.build()).toList())
				.build();
	}

	private List<Arkivendring> berikArkivendringer(List<Arkivendring> journalpost, Map<String, String> navAnsatteNavn) {
		return journalpost.stream()
				.map(arkivendring -> arkivendring.toBuilder()
						.utfoertAvBeriketNavn(utledNavn(arkivendring.getUtfoertAv(), navAnsatteNavn))
						.build())
				.toList();
	}

	private List<FilDetaljer> berikMedFil(Map<String, String> navAnsatteNavn, DokumentInfo dokumentInfo) {
		return dokumentInfo.getFd().stream()
				.map(filDetaljer -> {
					byte[] PDFA_fil = asposeService.convertToPDFA(filDetaljer.getFil(), dokumentInfo.getId());
					return filDetaljer.toBuilder()
							.opprettetAvBeriketNavn(utledNavn(filDetaljer.getOpprettetAv(), navAnsatteNavn))
							.filstorrelseBeriket(filDetaljer.getFil().length)
							.fil(PDFA_fil)
							.sha256hashBeriket(DigestUtils.sha256Hex(PDFA_fil))
							.build();
				})
				.toList();
	}

	private BrukerMedNavnedata mapBruker(Bruker bruker, Map<String, BrukerMedNavnedata> pdlHentIdenterBolks, Map<String, BrukerMedNavnedata> eregOrganisasjonBolk) {
		final String brukerId = bruker.getId();
		if (bruker.isPerson()) {
			return pdlHentIdenterBolks.getOrDefault(brukerId, BrukerMedNavnedata.ukjentPerson(brukerId));
		} else {
			return eregOrganisasjonBolk.getOrDefault(brukerId, BrukerMedNavnedata.ukjentOrganisasjon(brukerId));
		}
	}

	private String utledNavn(final String adeoIdent, final Map<String, String> navAnsatteNavn) {
		return navAnsatteNavn.getOrDefault(adeoIdent, AUTOMATISK_JOBB);
	}
}
