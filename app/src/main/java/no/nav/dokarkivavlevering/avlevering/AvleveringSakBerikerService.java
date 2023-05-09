package no.nav.dokarkivavlevering.avlevering;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.consumer.activedirectory.NavActiveDirectoryConsumer;
import no.nav.dokarkivavlevering.avlevering.consumer.ereg.EregService;
import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
import no.nav.dokarkivavlevering.avlevering.domain.DokumentInfo;
import no.nav.dokarkivavlevering.avlevering.domain.FilDetaljer;
import no.nav.dokarkivavlevering.avlevering.domain.Journalpost;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AvleveringSakBerikerService {

	public static final Pattern ADEO_IDENT_PATTERN = Pattern.compile("^[a-zA-Z]\\d{6}$");
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final NavActiveDirectoryConsumer navActiveDirectoryConsumer;
	private final EregService eregService;
	private final AvleveringSakBerikerMapper avleveringSakBerikerMapper;

	public AvleveringSakBerikerService(PdlGraphQLConsumer pdlGraphQLConsumer,
									   NavActiveDirectoryConsumer navActiveDirectoryConsumer,
									   EregService eregService, AvleveringSakBerikerMapper avleveringSakBerikerMapper) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.navActiveDirectoryConsumer = navActiveDirectoryConsumer;
		this.eregService = eregService;
		this.avleveringSakBerikerMapper = avleveringSakBerikerMapper;
	}

	public List<Sak> berikSakerMedDokumenter(@Body final List<Sak> saker, @ExchangeProperty(AvleveringRoute.PROPERTY_TEMA) final Tema tema) {
		return doBerikSaker(saker, tema, true);
	}

	public List<Sak> berikSakerUtenDokumenter(@Body final List<Sak> saker, @ExchangeProperty(AvleveringRoute.PROPERTY_TEMA) final Tema tema) {
		return doBerikSaker(saker, tema, false);
	}

	private List<Sak> doBerikSaker(List<Sak> saker, final Tema tema, boolean avleverDokumenter){
		// hent metadata og berik modellen
		log.info("Beriker metadata for {} saker med tema={}", saker.size(), tema);
		return Flowable.fromIterable(saker)
				.buffer(100)
				.parallel(10)
				.runOn(Schedulers.io())
				.map(saks -> {
					final Set<String> adeoIdenter = adeoIdenter(saks);
					final Map<String, String> navAnsatteNavn = navActiveDirectoryConsumer.hentNavAnsattBolk(adeoIdenter);
					final Set<String> unikeAktoerids = saks.stream()
							.filter(s -> s.getBruker().isPerson())
							.map(s -> s.getBruker().getId())
							.collect(Collectors.toSet());
					final Map<String, BrukerMedNavnedata> pdlHentIdenterBolks = pdlGraphQLConsumer.hentPersonBolk(unikeAktoerids, tema.getTemakode());
					final Set<String> unikeOrgnr = saks.stream()
							.filter(s -> s.getBruker().isOrganisasjon())
							.map(s -> s.getBruker().getId())
							.collect(Collectors.toSet());
					final Map<String, BrukerMedNavnedata> eregOrganisasjonBolk = eregService.hentOrganisasjonBrukere(unikeOrgnr);
					if(avleverDokumenter) {
						return saks.stream()
								.map(sak -> avleveringSakBerikerMapper.berikMedDokumenter(sak, navAnsatteNavn, pdlHentIdenterBolks, eregOrganisasjonBolk))
								.collect(Collectors.toList());
					} else {
						return saks.stream()
								.map(sak -> avleveringSakBerikerMapper.berikUtenDokument(sak, navAnsatteNavn, pdlHentIdenterBolks, eregOrganisasjonBolk))
								.collect(Collectors.toList());
					}
				})
				.flatMapIterable(items -> items)
				.sequential()
				.toList().subscribeOn(Schedulers.io()).blockingGet();
	}

	private Set<String> adeoIdenter(final List<Sak> saker) {
		Set<String> adeoIdenter = new HashSet<>();
		for (Sak sak : saker) {
			adeoIdenter.add(sak.getOpprettetAv());
			for (Journalpost journalpost : sak.getJp()) {
				adeoIdenter.add(journalpost.getOpprettetAv());
				adeoIdenter.add(journalpost.getEndretAv());
				for (Arkivendring journalpostArkivendring : journalpost.getAe()) {
					adeoIdenter.add(journalpostArkivendring.getUtfoertAv());
				}
				for (DokumentInfo dokumentInfo : journalpost.getDok()) {
					for (Arkivendring dokumentInfoArkivendring : dokumentInfo.getAe()) {
						adeoIdenter.add(dokumentInfoArkivendring.getUtfoertAv());
					}
					for (FilDetaljer filDetaljer : dokumentInfo.getFd()) {
						adeoIdenter.add(filDetaljer.getOpprettetAv());
					}
				}
			}
		}
		return adeoIdenter.stream()
				.filter(Objects::nonNull)
				.filter(s -> ADEO_IDENT_PATTERN.matcher(s).matches())
				.collect(Collectors.toSet());
	}

}
