package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Profile("avsluttSaker")
public interface ArbeidssakRepository extends JpaRepository<Arbeidssak, Long> {

	@Query("""
			select arbeidssak.sakId from Arbeidssak arbeidssak
			where arbeidssak.status is null or arbeidssak.status = "AAPEN"
			""")
	List<Long> findAllSakIds();

	List<Arbeidssak> findSaksBySakIdIn(List<Long> sakIds);

	@Query("""
			select arbeidssak from Arbeidssak arbeidssak where
			arbeidssak.aktoerId = :aktoerId and
			arbeidssak.fagsaknr = :fagsaknr and
			arbeidssak.applikasjon = :applikasjon
			""")
	List<Arbeidssak> findArkivsakForAktoerId(
			@Param("aktoerId") String aktoerId,
			@Param("fagsaknr") String fagsaknr,
			@Param("applikasjon") String applikasjon);

	@Query("""
			select arbeidssak from Arbeidssak arbeidssak
			where arbeidssak.aktoerId = :aktoerId
			and arbeidssak.applikasjon = :applikasjon
			and arbeidssak.fagsaknr is null
			""")
	List<Arbeidssak> findArkivsakForAktoerIdWhereFagsaknrIsNull(
			@Param("aktoerId") String aktoerId,
			@Param("applikasjon") String applikasjon);
}