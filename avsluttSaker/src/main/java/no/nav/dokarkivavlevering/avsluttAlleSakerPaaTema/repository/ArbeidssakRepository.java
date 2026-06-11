package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Profile("avsluttSaker")
public interface ArbeidssakRepository extends JpaRepository<Arbeidssak, Long> {

	@Query("""
			select arbeidssak.sakId from Arbeidssak arbeidssak
			where arbeidssak.arbeidsstatus is null
			and (arbeidssak.status is null or arbeidssak.status = 'AAPEN')
			and arbeidssak.aktoerId is not null
			order by arbeidssak.sakId asc
			""")
	List<Long> hentAlleUbehandledeSakerMedAktoerId();

	List<Arbeidssak> findSaksBySakIdIn(List<Long> sakIds);

	List<Arbeidssak> findSaksByAktoerIdIn(List<String> aktoerIds);
	List<Arbeidssak> findSaksByOrgnrIn(List<String> orgnrs);

	@Query("""
			select distinct(arbeidssak.aktoerId) from Arbeidssak arbeidssak
			where arbeidssak.aktoerId is not null
			and arbeidssak.arbeidsstatus = 'HENTET_FRA_PDL'
			order by arbeidssak.aktoerId asc
			""")
	List<String> hentOppdaterteAktoerIder();

	@Query("""
			select distinct(arbeidssak.orgnr) from Arbeidssak arbeidssak
			where arbeidssak.orgnr is not null
			and arbeidssak.arbeidsstatus is null
			order by arbeidssak.orgnr asc
			""")
	List<String> hentOrgnrs();

	@Query("""
			select arbeidssak.arbeidsstatus, count(arbeidssak)
			from Arbeidssak arbeidssak
			group by arbeidssak.arbeidsstatus
			order by count(arbeidssak) desc
			""")
	List<Object[]> tellAntallArbeidssakerForHverArbeidsstatus();
}