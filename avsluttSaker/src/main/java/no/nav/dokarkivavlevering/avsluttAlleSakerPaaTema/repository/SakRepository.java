package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

@Profile("avsluttSaker")
public interface SakRepository extends JpaRepository<Sak, Long> {

	@Query("""
			select sak.sakId from Sak sak
			where sak.status is null or sak.status = "AAPEN"
			""")
	List<Long> findAllSakIds();

	@Query("select distinct sak.aktoerId from Sak sak")
	Set<String> findAllAktoerIds();

	List<Sak> findSaksBySakIdIn(List<Long> sakIds);

	@Query("""
			select sak from Sak sak where
			sak.aktoerId = :aktoerId and
			sak.fagsaknr = :fagsaknr and
			sak.applikasjon = :applikasjon
			""")
	List<Sak> findArkivsakForAktoerId(
			@Param("aktoerId") String aktoerId,
			@Param("fagsaknr") String fagsaknr,
			@Param("applikasjon") String applikasjon);

	@Query("""
			select sak from Sak sak
			where sak.aktoerId = :aktoerId
			and sak.applikasjon = :applikasjon
			and sak.fagsaknr is null
			""")
	List<Sak> findArkivsakForAktoerIdWhereFagsaknrIsNull(
			@Param("aktoerId") String aktoerId,
			@Param("applikasjon") String applikasjon);
}