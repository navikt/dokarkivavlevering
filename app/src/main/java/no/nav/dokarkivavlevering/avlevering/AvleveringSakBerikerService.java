package no.nav.dokarkivavlevering.avlevering;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.consumer.activedirectory.NavActiveDirectoryConsumer;
import no.nav.dokarkivavlevering.avlevering.consumer.ereg.EregService;
import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Arkivendring;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
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
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Service
public class AvleveringSakBerikerService {

	public static final Pattern ADEO_IDENT_PATTERN = Pattern.compile("^[a-zA-Z]\\d{6}$");
	private static final String AUTOMATISK_JOBB = "Automatisk Jobb";
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final NavActiveDirectoryConsumer navActiveDirectoryConsumer;
	private final EregService eregService;

	public AvleveringSakBerikerService(PdlGraphQLConsumer pdlGraphQLConsumer,
									   NavActiveDirectoryConsumer navActiveDirectoryConsumer,
									   EregService eregService) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.navActiveDirectoryConsumer = navActiveDirectoryConsumer;
		this.eregService = eregService;
	}

	public List<Sak> berikSaker(@Body final List<Sak> saker, @ExchangeProperty(AvleveringRoute.PROPERTY_TEMA) final Tema tema) {
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
					final Map<String, Bruker> pdlHentIdenterBolks = pdlGraphQLConsumer.hentPersonBolk(unikeAktoerids, tema.getTemakode());
					final Set<String> unikeOrgnr = saks.stream()
							.filter(s -> s.getBruker().isOrganisasjon())
							.map(s -> s.getBruker().getId())
							.collect(Collectors.toSet());
					final Map<String, Bruker> eregOrganisasjonBolk = eregService.hentOrganisasjonBrukere(unikeOrgnr);
					return saks.stream()
							.map(sak -> {
								if (sak.getBruker().isPerson()) {
									return sak.tilhoererBruker(pdlHentIdenterBolks.getOrDefault(sak.getBruker().getId(),
											Bruker.ukjentPerson(sak.getBruker().getId())));
								} else {
									return sak.tilhoererBruker(eregOrganisasjonBolk.getOrDefault(sak.getBruker().getId(),
											Bruker.ukjentOrganisasjon(sak.getBruker().getId())));
								}
							})
							.map(sak -> navneberiketSak(sak, navAnsatteNavn))
							.collect(Collectors.toList());
				})
				.flatMapIterable(items -> items)
				.sequential()
				.toList().subscribeOn(Schedulers.io()).blockingGet();
	}

	private Set<String> adeoIdenter(final List<Sak> saker) {
		Set<String> adeoIdenter = new HashSet<>();
		for (Sak sak : saker) {
			adeoIdenter.add(sak.getOpprettetAv());
			for (Journalpost journalpost : sak.getJournalposter()) {
				adeoIdenter.add(journalpost.getOpprettetAv());
				adeoIdenter.add(journalpost.getEndretAv());
				for (Arkivendring journalpostArkivendring : journalpost.getArkivendringer()) {
					adeoIdenter.add(journalpostArkivendring.getUtfoertAv());
				}
				for (DokumentInfo dokumentInfo : journalpost.getDokumenter()) {
					for (Arkivendring dokumentInfoArkivendring : dokumentInfo.getArkivendringer()) {
						adeoIdenter.add(dokumentInfoArkivendring.getUtfoertAv());
					}
					for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljer()) {
						adeoIdenter.add(filDetaljer.getOpprettetAv());
					}
				}
			}
		}
		return adeoIdenter.stream().filter(s -> ADEO_IDENT_PATTERN.matcher(s).matches()).collect(Collectors.toSet());
	}

	private Sak navneberiketSak(final Sak sak, final Map<String, String> navAnsatteNavn) {
		return sak.toBuilder()
				.opprettetAvBeriketNavn(utledNavn(sak.getOpprettetAv(), navAnsatteNavn))
				.journalposter(sak.getJournalposter().stream().map(journalpost -> {
					return journalpost.toBuilder()
							.opprettetAvBeriketNavn(utledNavn(journalpost.getOpprettetAv(), navAnsatteNavn))
							.endretAv(utledNavn(journalpost.getEndretAv(), navAnsatteNavn))
							.dokumenter(journalpost.getDokumenter().stream()
									.map(dokumentInfo -> {
										return dokumentInfo.toBuilder()
												.opprettetAvBeriketNavn(utledNavn(dokumentInfo.getOpprettetAv(), navAnsatteNavn))
												.relasjonOpprettetAvBeriketNavn(utledNavn(dokumentInfo.getRelasjonOpprettetAv(), navAnsatteNavn))
												.fildetaljer(dokumentInfo.getFildetaljer().stream()
														.map(filDetaljer -> {
															return filDetaljer.toBuilder()
																	.opprettetAvBeriketNavn(utledNavn(filDetaljer.getOpprettetAv(), navAnsatteNavn))
																	.build();
														})
														.collect(Collectors.toList()))
												.arkivendringer(dokumentInfo.getArkivendringer().stream()
														.map(arkivendring -> {
															return arkivendring.toBuilder()
																	.utfoertAvBeriketNavn(utledNavn(arkivendring.getUtfoertAv(), navAnsatteNavn))
																	.build();
														})
														.collect(Collectors.toList()))
												.build();
									})
									.collect(Collectors.toList()))
							.arkivendringer(journalpost.getArkivendringer().stream()
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

	private String utledNavn(final String adeoIdent, final Map<String, String> navAnsatteNavn) {
		return navAnsatteNavn.getOrDefault(adeoIdent, AUTOMATISK_JOBB);
	}
}
