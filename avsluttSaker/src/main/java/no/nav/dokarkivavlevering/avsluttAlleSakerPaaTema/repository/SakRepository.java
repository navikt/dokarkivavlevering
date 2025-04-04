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
			select sak.sakId FROM Sak sak where sak.status is null OR sak.status = "AAPEN"
			""")
	List<Long> findAllSakIds();

	@Query("select distinct sak.aktoerId FROM Sak sak")
	Set<String> findAllAktoerIds();

	List<Sak> findSaksBySakIdIn(List<Long> sakIds);

	List<Sak> findSaksByAktoerId(String aktoerId);

	@Query("""
			select sak FROM Sak sak where
			sak.aktoerId = :aktoerId AND
			sak.fagsaknr = :fagsaknr AND
			sak.applikasjon = :applikasjon
			""")
	List<Sak> findArkivsakForAktoerId(
			@Param("aktoerId") String aktoerId,
			@Param("fagsaknr") String fagsaknr,
			@Param("applikasjon") String applikasjon);
}