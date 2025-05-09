package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.EnumSet;
import java.util.List;

@Profile("avsluttSaker")
public interface ArbeidssakRepository extends JpaRepository<Arbeidssak, Long> {

	@Query("""
			select arbeidssak.sakId from Arbeidssak arbeidssak
			where (arbeidssak.arbeidsstatus is null or arbeidssak.arbeidsstatus not in (:endeligeStatuser))
			and (arbeidssak.status is null or arbeidssak.status = "AAPEN")
			order by arbeidssak.sakId asc
			""")
	List<Long> findAllSakIdsWhereStatusIsNullOrAapen(EnumSet<Arbeidsstatus> endeligeStatuser);

	List<Arbeidssak> findSaksBySakIdIn(List<Long> sakIds);

	List<Arbeidssak> findSaksByAktoerIdIn(List<String> aktoerIds);
	List<Arbeidssak> findSaksByOrgnrIn(List<String> orgnrs);

	@Query("""
			select distinct(arbeidssak.aktoerId) from Arbeidssak arbeidssak
			where arbeidssak.aktoerId is not null
			and (arbeidssak.arbeidsstatus is null or arbeidssak.arbeidsstatus not in (:endeligeStatuser))
			order by arbeidssak.aktoerId asc
			""")
	List<String> findDistinctAktoerIds(EnumSet<Arbeidsstatus> endeligeStatuser);

	@Query("""
			select distinct(arbeidssak.orgnr) from Arbeidssak arbeidssak
			where arbeidssak.orgnr is not null
			and (arbeidssak.arbeidsstatus is null or arbeidssak.arbeidsstatus not in (:endeligeStatuser))
			order by arbeidssak.orgnr asc
			""")
	List<String> findDistinctOrgnrs(EnumSet<Arbeidsstatus> endeligeStatuser);

	@Query("""
			select arbeidssak.arbeidsstatus, count(arbeidssak)
			from Arbeidssak arbeidssak
			group by arbeidssak.arbeidsstatus
			order by count(arbeidssak) desc
			""")
	List<Object[]> tellAntallArbeidssakerForHverArbeidsstatus();
}