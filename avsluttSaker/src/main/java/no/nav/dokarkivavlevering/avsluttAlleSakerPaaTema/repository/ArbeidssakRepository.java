package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.EnumSet;
import java.util.List;

@Profile("avsluttSaker")
public interface ArbeidssakRepository extends JpaRepository<Arbeidssak, Long> {

	@Query("""
			select arbeidssak.sakId from Arbeidssak arbeidssak
			where arbeidssak.status is null or arbeidssak.status = "AAPEN"
			""")
	List<Long> findAllSakIdsWhereStatusIsNullOrAapen();

	List<Arbeidssak> findSaksBySakIdIn(List<Long> sakIds);

	List<Arbeidssak> findSaksByAktoerIdIn(List<String> aktoerIds);
	List<Arbeidssak> findSaksByOrgnrIn(List<String> orgnrs);

	@Query("""
			select distinct(arbeidssak.aktoerId) from Arbeidssak arbeidssak
			where arbeidssak.aktoerId is not null
			and arbeidssak.arbeidsstatus not in (:endeligeStatuser)
			""")
	List<String> findDistinctAktoerIds(EnumSet<Arbeidsstatus> endeligeStatuser);

	@Query("""
			select distinct(arbeidssak.orgnr) from Arbeidssak arbeidssak
			where arbeidssak.orgnr is not null
			and arbeidssak.arbeidsstatus not in (:endeligeStatuser)
			""")
	List<String> findDistinctOrgnrs(EnumSet<Arbeidsstatus> endeligeStatuser);
}